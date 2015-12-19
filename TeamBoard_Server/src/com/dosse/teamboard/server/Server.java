/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.dosse.teamboard.server;

import com.dosse.teamboard.protocol.ClearCanvasMessage;
import com.dosse.teamboard.protocol.DotMessage;
import com.dosse.teamboard.protocol.ImageMessage;
import com.dosse.teamboard.protocol.LineMessage;
import com.dosse.teamboard.protocol.Message;
import com.dosse.teamboard.renderer.Renderer;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import org.teleal.cling.UpnpService;
import org.teleal.cling.UpnpServiceImpl;
import org.teleal.cling.support.igd.PortMappingListener;
import org.teleal.cling.support.model.PortMapping;

/**
 *
 * @author dosse
 */
public class Server extends Thread {

    private ServerSocket ss; //we'll use this ServerSocket to listen for incoming connections (TCP)
    private ArrayList<Connection> connections = new ArrayList<>(); //list of currently connected clients

    private BufferedImage b; //the canvas

    private boolean dead = false;
    public boolean stopASAP = false; //set to true to terminate connection and thread

    /**
     * starts the server. can be stopped by terminating this thread and all its
     * children, or by terminating the whole program
     *
     * @param port port to listen on
     * @param w canvas width
     * @param h canvas height
     * @param upnp use upnp for NAT port forwarding
     * @throws Exception if anything goes wrong while starting the server
     */
    public Server(int port, int w, int h, boolean upnp) throws Exception {
        if (upnp) { //if needed, set up upnp NAT port forwarding
            Logger.addToLog("Setting NAT port forwarding...");
            //first we need the address of this machine on the local network
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            String ipAddress = null;
            Enumeration<NetworkInterface> net = null;
            try {
                net = NetworkInterface.getNetworkInterfaces();
            } catch (SocketException e) {
                throw new Exception("Not connected to any network");
            }

            while (net.hasMoreElements()) {
                NetworkInterface element = net.nextElement();
                Enumeration<InetAddress> addresses = element.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress ip = addresses.nextElement();
                    if (ip instanceof Inet4Address) {
                        if (ip.isSiteLocalAddress()) {
                            ipAddress = ip.getHostAddress();
                            break;
                        }
                    }
                }
                if (ipAddress != null) {
                    break;
                }
            }
            if (ipAddress == null) {
                throw new Exception("Not connected to any IPv4 network");
            }
            UpnpService u = new UpnpServiceImpl(new PortMappingListener(new PortMapping(port, ipAddress, PortMapping.Protocol.TCP)));
            u.getControlPoint().search();
        }
        Logger.addToLog("Initializing " + w + "x" + h + " canvas...");
        b = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB); //initialize canvas
        ss = new ServerSocket(port); //start listening
        Logger.addToLog("Server started on port " + port + "!");
        //this thread manages all incoming connections
        new Thread() {
            @Override
            public void run() {
                for (;;) {
                    if (stopASAP) {
                        dead = true;
                        return;
                    }
                    try {
                        Socket s = ss.accept(); //wait for connections
                        Logger.addToLog("Incoming connection: " + s.getInetAddress());
                        Connection c = new Connection(s); //there's a new client, we must initialize all its data
                        synchronized (b) {
                            c.write(new ImageMessage(b)); //we also need to send it the current state
                        }
                        synchronized (connections) {
                            connections.add(c); //and now we can add it to the list of clients
                        }
                    } catch (IOException ex) {
                        //something went wrong (ignore if termination requested)
                        if (!stopASAP) {
                            Logger.addToLog("Connection failed :(");
                        }
                    }
                }
            }
        }.start();
        start();
    }

    /**
     * stats about connected clients
     *
     * @return a String matrix, Nx6, where N is the number of connected clients,
     * and for each client, there are 6 stats: hostname, connection status (DEAD
     * or ALIVE), IN queue length, OUT queue length, IN total, OUT total (in
     * Messages)
     */
    public String[][] getConnectionStats() {
        synchronized (connections) {
            String[][] s = new String[connections.size()][6];
            for (int i = 0; i < connections.size(); i++) {
                s[i][0] = connections.get(i).getInetAddress();
                s[i][1] = connections.get(i).isDead() ? "DEAD" : "ALIVE";
                s[i][2] = "" + connections.get(i).getInQueueSize();
                s[i][3] = "" + connections.get(i).getOutQueueSize();
                s[i][4] = "" + connections.get(i).getInTotal();
                s[i][5] = "" + connections.get(i).getOutTotal();
            }
            return s;
        }
    }

    @Override
    public void run() {
        long lastCleanup = System.nanoTime();
        for (;;) {
            if (stopASAP) { //termination requested
                Logger.addToLog("Terminating server, please wait...");
                synchronized (connections) {
                    for (Connection x : connections) {
                        x.stopASAP = true;
                    }
                    connections.clear();
                }
                try {
                    ss.close();
                } catch (IOException ex) {
                }
                dead = true;
                Logger.addToLog("Server terminated");
                return;
            }
            if (System.nanoTime() - lastCleanup >= 10000000000L) { //every 10 seconds, clean up dead connections
                Logger.addToLog("Cleaning up...");
                synchronized (connections) {
                    ArrayList<Connection> deadConns = new ArrayList<>();
                    for (Connection x : connections) {
                        if (x.isDead()) {
                            deadConns.add(x);
                        }
                    }
                    connections.removeAll(deadConns);
                }
                Runtime.getRuntime().gc(); //and also run the GC to free some memory
                lastCleanup = System.nanoTime();
            }
            long t = System.nanoTime();
            ArrayList<Message> toBroadcast = new ArrayList<>();
            //for each client, we'll receive all incoming Messages and add them to the broadcast queue
            synchronized (connections) {
                for (Connection x : connections) {
                    while (true) {
                        Message m = x.read();
                        if (m == null) { //no more messages
                            break;
                        } else { //something to broadcast
                            if (!(m instanceof ImageMessage)) {
                                toBroadcast.add(m); //do not broadcast ImageMessages, as they shouldn't be sent by clients
                            }
                        }
                    }
                }
            }
            //before broadcasting the Messages, we must apply the changes to the canvas
            synchronized (b) {
                for (Message in : toBroadcast) {
                    if (in instanceof ClearCanvasMessage) { //clear canvas
                        Renderer.clear(b);
                    }
                    if (in instanceof DotMessage) { //a dot
                        DotMessage d = (DotMessage) in;
                        short x = (short) (d.getX() < 0 ? 0 : d.getX() >= b.getWidth() ? b.getWidth() - 1 : d.getX());
                        short y = (short) (d.getY() < 0 ? 0 : d.getY() >= b.getHeight() ? b.getHeight() - 1 : d.getY());
                        Renderer.drawDot(b, x, y, d.getRgb(), d.getW());
                    }
                    if (in instanceof LineMessage) { //a line
                        LineMessage l = (LineMessage) in;
                        short xa = l.getXa();
                        xa = (short) (xa < 0 ? 0 : xa >= b.getWidth() ? b.getWidth() - 1 : xa);
                        short xb = l.getXb();
                        xb = (short) (xb < 0 ? 0 : xb >= b.getWidth() ? b.getWidth() - 1 : xb);
                        short ya = l.getYa();
                        ya = (short) (ya < 0 ? 0 : ya >= b.getHeight() ? b.getHeight() - 1 : ya);
                        short yb = l.getYb();
                        yb = (short) (yb < 0 ? 0 : yb >= b.getHeight() ? b.getHeight() - 1 : yb);
                        Renderer.drawLine(b, xa, ya, xb, yb, l.getRgb(), l.getW());
                    }
                }
            }
            //finally, we can broadcast the messages
            synchronized (connections) {
                for (Connection x : connections) {
                    for (Message m : toBroadcast) {
                        x.write(m);
                    }
                }
            }
            //now we'll avoid busy wait
            long t2 = System.nanoTime(), diff = t2 - t;
            if (diff < Settings.cycleTime_Server * 1000000L) { //took less than cycleTime to compute
                int msToWait = (int) (Settings.cycleTime_Server - (diff / 1000000L));
                diff %= 1000000L;
                try {
                    Thread.sleep(msToWait, (int) (1000000 - diff));  //wait until remaining cycleTime has past
                } catch (Throwable ex) {
                }
            }
        }
    }

    /**
     * has the server been stopped?
     *
     * @return
     */
    public boolean isDead() {
        return dead;
    }

}
