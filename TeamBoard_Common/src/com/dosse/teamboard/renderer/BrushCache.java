/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.dosse.teamboard.renderer;

import java.util.ArrayList;

/**
 *
 * @author dosse
 */
public class BrushCache {

    private static final ArrayList<BrushCacheEntry> cache = new ArrayList<>();
    private static final boolean[][] errBrush = new boolean[1][1];

    public static boolean[][] getBrush(int w) {
        try {
            for (BrushCacheEntry e : cache) {
                if (e.getBrushSize() == w) {
                    return e.getPixels();
                }
            }
            BrushCacheEntry b = new BrushCacheEntry(w);
            cache.add(b);
            return b.getPixels();
        } catch (Throwable t) {
            return errBrush;
        }
    }

}
