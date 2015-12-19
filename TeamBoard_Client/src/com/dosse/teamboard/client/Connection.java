/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.dosse.teamboard.client;

import com.dosse.teamboard.protocol.Message;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;

/**
 *
 * @author dosse
 */
public class Connection extends Thread {

    private Socket s; //connection to server
    private ObjectInputStream in; //object input stream to read incoming Messages
    private ObjectOutputStream out; //object output stream to write Messages

    private boolean dead = false; //set to true when the connection is no longer active

    private final ArrayList<Message> inQueue = new ArrayList<>(); //queue for incoming messages
    private final ArrayList<Message> outQueue = new ArrayList<>(); //queue for outgoing messages

    public boolean stopASAP = false; //set to true to terminate connection and thread

    /**
     * create the connection
     *
     * @param host server address
     * @param port server port
     * @throws IOException if anything goes wrong
     */
    public Connection(String host, int port) throws IOException {
        s = new Socket(host, port);
        out = new ObjectOutputStream(s.getOutputStream());
        in = new ObjectInputStream(s.getInputStream());
        start();
    }

    @Override
    public void run() {
        try {
            for (;;) {
                if (stopASAP) {
                    s.close();
                    dead = true;
                    return;
                } //termination requested
                //got something to read?
                while (s.getInputStream().available() > 0) {
                    //read and add to queue
                    Message m = (Message) (in.readObject());
                    synchronized (inQueue) {
                        inQueue.add(m);
                    }
                }
                //got something to send?
                boolean moreToSend = false;
                do {
                    Message toSend = null;
                    synchronized (outQueue) {
                        if (outQueue.size() > 0) {
                            //we got something to send, remove it from the queue and...
                            toSend = outQueue.get(0);
                            outQueue.remove(0);
                            moreToSend = outQueue.size() >= 1; //got something else to send? (can't put this condition in while statement because it needs mutex)
                        }
                    }
                    if (toSend != null) {
                        out.writeObject(toSend); //...send it
                    }
                } while (moreToSend);
                Thread.sleep(1); //brutal sleep to avoid busy wait, but a more elegant solution is implemented on the server
            }
        } catch (Throwable t) {
            //something went wrong (or the server was stopped)
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
     * send to server (adds to queue)
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
}
