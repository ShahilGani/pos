package POS;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import javax.imageio.ImageIO;

public class ImagePrinter {

    public static byte[] convertImageToESC_POS(String imagePath) {
        try {
            BufferedImage image = ImageIO.read(new File(imagePath));

            // Convert image to ESC/POS format
            int width = image.getWidth();
            int height = image.getHeight();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();

            // ESC/POS Command to start image printing
            baos.write(new byte[]{0x1B, 0x33, 0x00}); // Line spacing

            for (int y = 0; y < height; y += 24) { // Process 24 rows at a time
                baos.write(new byte[]{0x1B, 0x2A, 33, (byte) (width % 256), (byte) (width / 256)});
                for (int x = 0; x < width; x++) {
                    int slice = 0;
                    for (int bit = 0; bit < 24; bit++) {
                        if (y + bit < height) {
                            int pixel = image.getRGB(x, y + bit);
                            int grayscale = (pixel & 0xFF) < 128 ? 1 : 0; // Convert to black & white
                            slice |= (grayscale << (7 - bit));
                        }
                    }
                    baos.write(slice);
                }
                baos.write(0x0A); // New line
            }

            baos.write(new byte[]{0x1B, 0x32}); // Reset line spacing
            return baos.toByteArray();

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}

