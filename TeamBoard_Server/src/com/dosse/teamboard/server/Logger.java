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
public class Logger {

    private static String log = ""; //the log
    public static boolean outputToTerminal = false;

    /**
     * adds a line to the log (\n not required)
     *
     * @param s the line to add
     */
    public static void addToLog(String s) {
        synchronized (Logger.class) {
            log += s + "\n";
            if (outputToTerminal) {
                System.out.println(s);
            }
        }
    }

    /**
     * get the log string
     *
     * @return
     */
    public static String getLog() {
        synchronized (Logger.class) {
            return log;
        }
    }
}
