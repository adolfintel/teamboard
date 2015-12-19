/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.dosse.teamboard.server;

import com.dosse.teamboard.protocol.Message;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;

/**
 *
 * @author dosse
 */
public class Connection extends Thread {

    private Socket s; //connection to client
    private ObjectInputStream in; //object input stream to read incoming Messages
    private ObjectOutputStream out; //object output stream to write Messages

    private boolean dead = false; //set to true when the connection is no longer active

    private ArrayList<Message> inQueue = new ArrayList<>(); //queue for incoming messages
    private ArrayList<Message> outQueue = new ArrayList<>(); //queue for outgoing messages
    private int inTotal = 0, outTotal = 0; //total number of received Messages (for stats only)
    private Object lock = new Object(); //used for mutex

    public boolean stopASAP = false; //set to true to terminate connection and thread

    /**
     * create the connection
     *
     * @param s client socket
     */
    public Connection(Socket s) {
        this.s = s;
        start();
    }

    @Override
    public void run() {
        if (Settings.connectionsHaveHighPriority) {
            setPriority(Thread.MAX_PRIORITY);
        }
        try {
            long lastHeartbeat = System.nanoTime(); //heartbeat = dummy Message sent from server to see if the client is still alive
            in = new ObjectInputStream(s.getInputStream());
            out = new ObjectOutputStream(s.getOutputStream());
            Logger.addToLog("Connection established: " + s.getInetAddress());
            for (;;) {
                if (stopASAP) { //termination requested
                    Logger.addToLog("Terminating connection: " + s.getInetAddress());
                    s.close();
                    dead = true;
                    return;
                }
                long t = System.nanoTime();
                //got something to read?
                while (s.getInputStream().available() > 0) {
                    //read and add to queue
                    Message m = (Message) (in.readObject());
                    synchronized (lock) {
                        inTotal++;
                    }
                    synchronized (inQueue) {
                        inQueue.add(m);
                    }
                }
                //got something to send?
                boolean moreToSend = false;
                do {
                    Message toSend = null;
                    synchronized (outQueue) {
                        if (System.nanoTime() - lastHeartbeat > 1000000000L) { // 1 heartbeat/second
                            outQueue.add(new Message()); //empty message to detect if client is dead
                            lastHeartbeat = System.nanoTime();
                        }
                        if (outQueue.size() > 0) {
                            //we got something to send, remove it from the queue and...
                            toSend = outQueue.get(0);
                            outQueue.remove(0);
                            moreToSend = outQueue.size()>=1; //got something else to send? (can't put this condition in while statement because it needs mutex)
                        }
                    }
                    if (toSend != null) {
                        out.writeObject(toSend); //...send it
                        synchronized (lock) {
                            outTotal++;
                        }
                    }
                } while (moreToSend);
                //now we'll avoid busy wait
                long t2 = System.nanoTime(), diff = t2 - t;
                if (diff < Settings.cycleTime_Connection * 1000000L) { //took less than cycleTime to compute
                    int msToWait = (int) (Settings.cycleTime_Connection - (diff / 1000000L));
                    diff %= 1000000L;
                    try {
                        Thread.sleep(msToWait, (int) (1000000 - diff));  //wait until remaining cycleTime has past
                    } catch (Throwable ex) {
                    }
                }
            }
        } catch (Throwable ex) {
            //something went wrong (or the client closed the connection)
            Logger.addToLog("Connection dead: " + s.getInetAddress());
            dead = true;
        }

    }

    /**
     * take the next message from the incoming queue
     *
     * @return the next available Message, or null if there's nothing new
     */
    public Message read() {
        Message m = null;
        synchronized (inQueue) {
            if (inQueue.size() > 0) {
                m = inQueue.get(0);
                inQueue.remove(0);
            }
        }
        return m;
    }

    /**
     * send to client (adds to queue)
     *
     * @param m the Message to send
     */
    public void write(Message m) {
        synchronized (outQueue) {
            outQueue.add(m);
        }
    }

    /**
     * is the connection dead?
     *
     * @return
     */
    public boolean isDead() {
        return dead;
    }

    /**
     * get the client's address
     *
     * @return the client's address
     */
    public String getInetAddress() {
        try {
            return s.getInetAddress().toString();
        } catch (Throwable t) {
            return "Socket closed";
        }
    }

    /**
     * get the size of the incoming queue
     *
     * @return
     */
    public int getInQueueSize() {
        synchronized (inQueue) {
            return inQueue.size();
        }
    }

    /**
     * get the size of the outgoing queue
     *
     * @return
     */
    public int getOutQueueSize() {
        synchronized (outQueue) {
            return outQueue.size();
        }
    }

    /**
     * get the total number of Messages received
     *
     * @return
     */
    public int getInTotal() {
        synchronized (lock) {
            return inTotal;
        }
    }

    /**
     * get the total number of Messages sent
     *
     * @return
     */
    public int getOutTotal() {
        synchronized (lock) {
            return outTotal;
        }
    }

}
