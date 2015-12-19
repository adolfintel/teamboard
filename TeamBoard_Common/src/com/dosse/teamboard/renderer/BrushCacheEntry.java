/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.dosse.teamboard.renderer;

/**
 *
 * @author dosse
 */
public class BrushCacheEntry {

    private boolean[][] pixels;
    private int brushSize;

    public BrushCacheEntry(int brushSize) {
        if (brushSize < 1 || brushSize > 64) {
            throw new RuntimeException("1<=brushSize<=64");
        }
        this.brushSize = brushSize;
        pixels = new boolean[brushSize * 2 + 1][brushSize * 2 + 1];
        int brushSizeSquared = brushSize * brushSize;
        int centerX = brushSize, centerY = brushSize;
        for (int y = 0; y < pixels.length; y++) {
            for (int x = 0; x < pixels[0].length; x++) {
                int dx = x - centerX, dy = y - centerY;
                pixels[y][x] = dx * dx + dy * dy <= brushSizeSquared;
            }
        }
    }

    public boolean[][] getPixels() {
        if (Settings.encapsulateBrushCacheEntryPixels) {
            boolean[][] pixelsCopy = new boolean[pixels.length][pixels[0].length];
            for (int y = 0; y < pixels.length; y++) {
                System.arraycopy(pixels[y], 0, pixelsCopy[y], 0, pixels[0].length);
            }
            return pixelsCopy;
        } else {
            return pixels;
        }
    }

    public int getBrushSize() {
        return brushSize;
    }

}
