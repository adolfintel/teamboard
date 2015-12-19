/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.dosse.teamboard.server;

/**
 *
 * @author dosse
 */
public class Settings {
    public static final int cycleTime_Server=50, cycleTime_Connection=25; //cycle times in ms (used to avoid busy waits)
    public static boolean connectionsHaveHighPriority=false; //if true, Connection Threads have higher priority than others, improving smoothness on client side, but the server will run slower, which may cause the client to be even slower.
}
