/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.dosse.teamboard.client;

import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.Timer;
import javax.swing.filechooser.FileFilter;

/**
 *
 * @author dosse
 */
public class GUI extends javax.swing.JFrame {

    private SyncBufferedImage image; //connection to server and image

    private short dragStartX = -1, dragStartY = -1; //used for dragging

    /**
     * starts the GUI
     *
     * @param host server address
     * @param port server port
     */
    public GUI(final String host, final int port) {
        initComponents();
        try {
            image = new SyncBufferedImage(host, port); //connect to server
        } catch (IOException ex) {
            //something went wrong
            JOptionPane.showMessageDialog(new JOptionPane(), ex, getTitle(), JOptionPane.ERROR_MESSAGE);
            System.exit(0);
        }
        setVisible(true);
        final JPanel c = new JPanel() { //this is where the image will be shown
            @Override
            public void paintComponent(Graphics g) {
                if (image == null) {
                    return;
                }
                long t = System.nanoTime();
                g.drawImage(image.getImage(), 0, 0, null);
                //now we'll limit the framerate
                if (Settings.reduceFramerate) {
                    while (System.nanoTime() - t < 20000000L /*20 ms*/) {
                        try {
                            Thread.sleep(0, 100000);
                        } catch (InterruptedException ex) {
                            Logger.getLogger(GUI.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                }
            }
        };
        add(c);
        //add listener for clicks
        c.addMouseListener(new MouseListener() {

            @Override
            public void mouseClicked(MouseEvent me) {
                if (me.getButton() == MouseEvent.BUTTON3) { //right click, save image
                    BufferedImage b = image.getImageCopy(); //gets a copy of the image (current state)
                    JFileChooser c = new JFileChooser();
                    c.setFileFilter(new FileFilter() {

                        @Override
                        public boolean accept(File file) {
                            return file.isDirectory() || file.getName().toLowerCase().endsWith(".png");
                        }

                        @Override
                        public String getDescription() {
                            return "PNG Images (*.png)";
                        }
                    });
                    c.showSaveDialog(rootPane);
                    File x = c.getSelectedFile();
                    if (x == null) {
                        return; //no file selected
                    }
                    if (!x.getName().toLowerCase().endsWith(".png")) {
                        x = new File(x.getAbsolutePath() + ".png");
                    }
                    try {//save to file
                        FileOutputStream fos = new FileOutputStream(x);
                        ImageIO.write(b, "png", fos);
                        fos.close();
                    } catch (Exception ex) {
                        //something went wrong
                        JOptionPane.showMessageDialog(new JOptionPane(), ex, getTitle(), JOptionPane.ERROR_MESSAGE);
                    }
                } else {
                    //left click, create dot
                    image.dot((short) me.getX(), (short) me.getY(), brush.getR() << 16 | brush.getG() << 8 | brush.getB(), (byte) brush.getW());
                }
            }

            @Override
            public void mousePressed(MouseEvent me) {
                dragStartX = (short) me.getX();
                dragStartY = (short) me.getY();
            }

            @Override
            public void mouseReleased(MouseEvent me) {
                dragStartX = -1;
                dragStartY = -1;
            }

            @Override
            public void mouseEntered(MouseEvent me) {
            }

            @Override
            public void mouseExited(MouseEvent me) {
            }
        });
        //add listener for drags
        c.addMouseMotionListener(new MouseMotionListener() {
            @Override
            public void mouseDragged(MouseEvent me) {
                short x2 = (short) me.getX(), y2 = (short) me.getY(), x1 = dragStartX, y1 = dragStartY;
                dragStartX = x2;
                dragStartY = y2;
                if (dragStartX == -1 || dragStartY == -1) { //prevents accidental drag events sometimes called when switching window
                    return;
                }
                image.line(x1, x2, y1, y2, brush.getR() << 16 | brush.getG() << 8 | brush.getB(), (byte) brush.getW());
            }

            @Override
            public void mouseMoved(MouseEvent me) {
            }
        });
        //every 20 ms, the drawable area is repainted. this timer also updates the size of the window.
        Timer repainter = new Timer(20, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                if (image.isDead()) { //something went wrong
                    JOptionPane.showMessageDialog(new JOptionPane(), "Connection lost", getTitle(), JOptionPane.ERROR_MESSAGE);
                    System.exit(0);
                }
                int w = image.getWidth() + getInsets().left + getInsets().right, h = image.getHeight() + getInsets().top + getInsets().bottom + 64;
                if (getWidth() != w || getHeight() != h) { //need to update window size
                    c.setSize(image.getWidth(), image.getHeight());
                    setSize(w, h);
                }
                c.repaint(); //repaint drawable area
            }
        });
        repainter.setRepeats(true);
        repainter.start();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        brush = new com.dosse.teamboard.client.BrushSettings();
        clear = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("TeamBoard");
        setResizable(false);

        clear.setText("Clear");
        clear.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                clearActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(brush, javax.swing.GroupLayout.DEFAULT_SIZE, 537, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(clear))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGap(0, 375, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(clear, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(brush, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void clearActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_clearActionPerformed
        image.clear();
    }//GEN-LAST:event_clearActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private com.dosse.teamboard.client.BrushSettings brush;
    private javax.swing.JButton clear;
    // End of variables declaration//GEN-END:variables
}
