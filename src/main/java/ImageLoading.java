import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Objects;

public class ImageLoading {

    private final HashMap<String, BufferedImage> bufferedImageCache = new HashMap<>(); // Cache for BufferedImages
    private final HashMap<String, ImageIcon> imageIconCache = new HashMap<>(); // Cache for ImageIcons


    // Loads image files
    public BufferedImage reader(String resource) {
        if (bufferedImageCache.containsKey(resource)) return bufferedImageCache.get(resource); // Checks if the path has already been loaded
        BufferedImage image = null;
        try {
            if (resource.endsWith(".png")) { // Checks if the image is a .png
                image = ImageIO.read(Files.newInputStream(Paths.get(resource))); // Image is local
                image = ImageIO.read(Objects.requireNonNull(getClass().getResource(resource))); // Image is in the JAR file
            } else throw new IllegalArgumentException("The image format is not supported: " + resource);

            bufferedImageCache.put(resource, image); // Adds the image to the cache

        } catch (IOException e) {
            e.printStackTrace();
        }
        if (image == null) throw new IllegalArgumentException("The image could not be loaded: " + resource);
        return image;
    }

    // Creates an ImageIcon from images
    public ImageIcon createImageIcon(String resource) {
        if (imageIconCache.containsKey(resource)) return imageIconCache.get(resource); // Checks if the path has already been loaded
        ImageIcon imageIcon;
        if (resource.endsWith(".png")) {
            imageIcon = new ImageIcon(reader(resource)); // Creates an ImageIcon
        } else if (resource.endsWith(".gif")) {
            URL imageUrl = getClass().getClassLoader().getResource(resource);
            imageIcon = new ImageIcon(Objects.requireNonNull(imageUrl));
        } else throw new IllegalArgumentException("The image format is not supported: " + resource);

        imageIconCache.put(resource, imageIcon); // Adds the image to the cache
        return imageIcon;
    }
}