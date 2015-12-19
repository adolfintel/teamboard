/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.dosse.teamboard.protocol;

/**
 * a line from xa,ya to xb,yb, with color rgb and w weight
 * @author dosse
 */
public class LineMessage extends Message {

    private short xa, xb, ya, yb; //coordinates A=from, B=to
    private int rgb; //color
    private byte w; //weight
    private final long serialVersionUID = 1L;
    /**
     * create a LineMessage
     * @param xa x coordinate (from)
     * @param xb x coordinate (to)
     * @param ya y coordinate (from)
     * @param yb y coordinate (to)
     * @param w weight (should be positive)
     * @param rgb color (eg. 0x00FF0000 is red, 0x00FFFF00 is yellow, ...)
     */
    public LineMessage(short xa, short xb, short ya, short yb, byte w, int rgb) {
        this.xa = xa;
        this.xb = xb;
        this.ya = ya;
        this.yb = yb;
        this.w = w;
        this.rgb = rgb;
    }
    /**
     * x coordinate (from)
     * @return 
     */
    public short getXa() {
        return xa;
    }
    /**
     * x coordinate (to)
     * @return 
     */
    public short getXb() {
        return xb;
    }
    /**
     * y coordinate (from)
     * @return 
     */
    public short getYa() {
        return ya;
    }
    /**
     * y coordinate (to)
     * @return 
     */
    public short getYb() {
        return yb;
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
