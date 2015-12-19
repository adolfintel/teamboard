/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.dosse.teamboard.protocol;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import javax.imageio.ImageIO;

/**
 * an Image, compressed in PNG
 * @author dosse
 */
public class ImageMessage extends Message {

    private byte[] compressedImage; //PNG or uncompressed bytes bytes
    private final long serialVersionUID = 1L;
    /**
     * create an ImageMessage (PNG encoding)
     * @param image the image
     */
    public ImageMessage(BufferedImage image) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(image, "png", baos);
            compressedImage = baos.toByteArray();
        } catch (Exception ex) {
            System.out.println(ex);
        }
    }
    /**
     * decompress the image and return as BufferedImage
     * @return 
     */
    public BufferedImage getImage() {
        try {
            ByteArrayInputStream bais = new ByteArrayInputStream(compressedImage);
            BufferedImage b = ImageIO.read(bais);
            return b;
        } catch (IOException ex) {
            System.out.println(ex);
        }
        return null;
    }

}
