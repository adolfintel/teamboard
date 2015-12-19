/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.dosse.teamboard.client;

/**
 *
 * @author dosse
 */
public class Settings {
    public static final boolean encapsulateSyncBufferedImageImage=false; //true="better oop", false=better performance
    public static final boolean reduceFramerate = true; //if set to true, it will reduce the framerate of the client GUI, in order to reduce the amount of Messages sent to the server. however, false will produce much smoother curves
}
