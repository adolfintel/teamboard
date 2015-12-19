/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.dosse.teamboard.protocol;

/**
 * a dot
 * @author dosse
 */
public class DotMessage extends Message {

    private short x, y; //coordinates
    private int rgb; //color
    private byte w; //weight
    private final long serialVersionUID = 1L;
    /**
     * creates a dot
     * @param x x coordinate
     * @param y y coordinate
     * @param w weight (should be positive)
     * @param rgb color
     */
    public DotMessage(short x, short y, byte w, int rgb) {
        this.x = x;
        this.y = y;
        this.w = w;
        this.rgb = rgb;
    }
    /**
     * x coordinate
     * @return 
     */
    public short getX() {
        return x;
    }
    /**
     * y coordinate
     * @return 
     */
    public short getY() {
        return y;
    }
    /**
     * get the color
     * @return 
     */
    public int getRgb() {
        return rgb;
    }
    /**
     * get the weight
     * @return 
     */
    public byte getW() {
        return w;
    }

}
