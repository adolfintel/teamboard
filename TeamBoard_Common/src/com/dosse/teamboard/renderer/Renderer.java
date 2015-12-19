/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.dosse.teamboard.renderer;

import java.awt.image.BufferedImage;

/**
 *
 * @author dosse
 */
public class Renderer {

    /**
     * draw a dot to specified BufferedImage at specified coordinates
     *
     * @param target
     * @param x
     * @param y
     * @param rgb color
     * @param w brush size
     */
    public static void drawDot(BufferedImage target, int x, int y, int rgb, int w) {
        if (w < 1) {
            return;
        }
        if (x < 0 || x >= target.getWidth() || y < 0 || y >= target.getHeight()) {
            return;
        }
        if (w == 1) { //radius = 1
            target.setRGB(x, y, rgb);
        } else { //radius > 1 
            boolean[][] px = BrushCache.getBrush(w);
            for (int yy = 0; yy < px.length; yy++) {
                for (int xx = 0; xx < px[0].length; xx++) {
                    if (!px[yy][xx]) {
                        continue;
                    }
                    int xInTarget = x - xx + px.length / 2, yInTarget = y - yy + px[0].length / 2;
                    if (!(xInTarget < 0 || xInTarget >= target.getWidth() || yInTarget < 0 || yInTarget >= target.getHeight())) {
                        target.setRGB(xInTarget, yInTarget, rgb);
                    }
                }
            }
        }
    }

    /**
     * draw a line to the specified BufferedImage
     *
     * @param target
     * @param xa
     * @param ya
     * @param xb
     * @param yb
     * @param rgb color
     * @param w brush size
     */
    public static void drawLine(BufferedImage target, int xa, int ya, int xb, int yb, int rgb, int w) {
        if (w < 1) {
            return;
        }
        if (xa < 0 || xa >= target.getWidth() || ya < 0 || ya >= target.getHeight()) {
            return;
        }
        if (xb < 0 || xb >= target.getWidth() || yb < 0 || yb >= target.getHeight()) {
            return;
        }
        int dx = xa - xb, dy = ya - yb;
        double len = Math.sqrt(dx * dx + dy * dy);
        if (len > 0) {
            for (double d = 0; d < len; d +=0.9) { //did not use d++ to avoid approx. errors that would leave gaps in the line
                double f = d / len;
                int x = (int) (xa * (1 - f) + xb * f), y = (int) (ya * (1 - f) + yb * f);
                //copy-pasted code from drawDot to speed things up
                if (w == 1) { //radius = 1
                    target.setRGB(x, y, rgb);
                } else { //radius > 1 
                    boolean[][] px = BrushCache.getBrush(w);
                    for (int yy = 0; yy < px.length; yy++) {
                        for (int xx = 0; xx < px[0].length; xx++) {
                            if (!px[yy][xx]) {
                                continue;
                            }
                            int xInTarget = x - xx + px.length / 2, yInTarget = y - yy + px[0].length / 2;
                            if (!(xInTarget < 0 || xInTarget >= target.getWidth() || yInTarget < 0 || yInTarget >= target.getHeight())) {
                                target.setRGB(xInTarget, yInTarget, rgb);
                            }
                        }
                    }
                }
            }
        }
    }
    /**
     * clear the BufferedImage
     * @param target 
     */
    public static void clear(BufferedImage target) {
        for (int y = 0; y < target.getHeight(); y++) {
            for (int x = 0; x < target.getWidth(); x++) {
                target.setRGB(x, y, 0);
            }
        }
    }

}
