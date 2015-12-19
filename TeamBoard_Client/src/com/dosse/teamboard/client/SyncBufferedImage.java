/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.dosse.teamboard.client;

import com.dosse.teamboard.protocol.ClearCanvasMessage;
import com.dosse.teamboard.protocol.DotMessage;
import com.dosse.teamboard.protocol.ImageMessage;
import com.dosse.teamboard.protocol.LineMessage;
import com.dosse.teamboard.protocol.Message;
import com.dosse.teamboard.renderer.Renderer;
import java.awt.image.BufferedImage;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import javax.imageio.ImageIO;

/**
 *
 * @author dosse
 */
public class SyncBufferedImage extends Thread {

    private BufferedImage b = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB); //the image
    private boolean imageInitialized = false;
    private final Object lock = new Object(); //used for mutex
    private Connection conn;
    public boolean stopASAP = false;
    private boolean dead = false;

    public SyncBufferedImage(String host, int port) throws IOException {
        conn = new Connection(host, port); //connect to server
        start();
    }

    @Override
    public void run() {
        try {
            while (!conn.isDead()) {
                if (stopASAP) { //termination requested
                    conn.stopASAP = true;
                    dead = true;
                    return;
                }
                Message in = conn.read();
                if (in != null) { //a message is available
                    if (in instanceof ImageMessage) { //it's an image (it should always be the first message)
                        synchronized (lock) {
                            b = ((ImageMessage) in).getImage();
                            imageInitialized = true;
                        }
                    }
                    if (imageInitialized) {
                        if (in instanceof ClearCanvasMessage) { //clear canvas
                            synchronized (lock) {
                                Renderer.clear(b);
                            }
                        }
                        if (in instanceof DotMessage) { //it's a dot
                            DotMessage d = (DotMessage) in;
                            short x = (short) (d.getX() < 0 ? 0 : d.getX() >= b.getWidth() ? b.getWidth() - 1 : d.getX());
                            short y = (short) (d.getY() < 0 ? 0 : d.getY() >= b.getHeight() ? b.getHeight() - 1 : d.getY());
                            synchronized (lock) {
                                Renderer.drawDot(b, x, y, d.getRgb(), d.getW());
                            }
                        }
                        if (in instanceof LineMessage) { //it's a line
                            LineMessage l = (LineMessage) in;
                            synchronized (lock) {
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
                }
                try {
                    Thread.sleep(1); //brutal 1ms sleep to avoid busy wait (that's fine for the client, but a more elegant solution is used on the server)
                } catch (InterruptedException ex) {
                }
            }
            //the server was stopped (or some other communication error)
            dead = true;
        } catch (Throwable t) {
            //something went wrong
            dead = true;
        }
    }

    public BufferedImage getImage() {
        if (!imageInitialized) {
            return new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
        }
        synchronized (lock) {
            if (Settings.encapsulateSyncBufferedImageImage) {
                return b.getSubimage(0, 0, b.getWidth(), b.getHeight()); //return a copy of the image
            } else {
                return b; //return the image itself
            }
        }
    }
    
    public BufferedImage getImageCopy(){
        return b.getSubimage(0, 0, b.getWidth(), b.getHeight());
    }

    public int getWidth() {
        if (!imageInitialized) {
            return 1;
        }
        synchronized (lock) {
            return b.getWidth();
        }
    }

    public int getHeight() {
        if (!imageInitialized) {
            return 1;
        }
        synchronized (lock) {
            return b.getHeight();
        }
    }

    public boolean isDead() {
        return dead;
    }

    public void dot(short x, short y, int rgb, byte w) {
        if (!dead) {
            conn.write(new DotMessage(x, y, w, rgb));
        }
    }

    public void line(short xa, short xb, short ya, short yb, int rgb, byte w) {
        if (!dead) {
            conn.write(new LineMessage(xa, xb, ya, yb, w, rgb));
        }
    }

    public void clear() {
        if (!dead) {
            conn.write(new ClearCanvasMessage());
        }
    }

    public void saveToFile(String path) throws FileNotFoundException, IOException {
        if (!path.toLowerCase().endsWith(".png")) {
            path += ".png";
        }
        FileOutputStream fos = new FileOutputStream(path);
        ImageIO.write(b, "png", fos);
        fos.flush();
        fos.close();
    }
}
