# Repository with common code solutions

## Table of Contents
- [Image Loading](#image-loading)
- [Play Audio](#play-audio)
- [Center Image](#center-image)
- [Center JFrame](#center-jframe)
- [Check connection to specified server](#check-connection-to-specified-server)
- [System latency](#system-latency)
- [Write a debug log file](#write-a-debug-log-file)
- [Read from a json file](#read-from-a-json-file)
- [Foreground color changer](#foreground-color-changer)

<br> <br>

### Image Loading

First of all, you need these imports:
```java
import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Objects;
```
<br>

You have to add these attributes to your class as cache for better performance:
```java
public class ImageLoader {
    private final HashMap<String, BufferedImage> bufferedImageCache = new HashMap<>(); // Cache for BufferedImages
    private final HashMap<String, ImageIcon> imageIconCache = new HashMap<>(); // Cache for ImageIcons
}
```
<br>

Create Buffered Images from local and jar resources:
```java 
public class ImageLoader {
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
}
```
<br>
For swing components you need ImageIcon, this requires the method above:

```java
public class ImageIconLoader {
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
```
<br>

### Play Audio
First of all, you need these imports:
```java
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineEvent;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.CompletableFuture;
```
<br>

You have to add these attributes to your class as cache for better performance:
```java
public class Audio {
    private final HashMap<String, Clip> HeavyClipCache = new HashMap<>(); // Cache for AudioClips
    private final ArrayList<BufferedInputStream> HeavyBufferedInputStreamCache = new ArrayList<>(); // Cache for BufferedInputStreams
    private final ArrayList<AudioInputStream> HeavyAudioInputStreamCache = new ArrayList<>(); // Cache for AudioInputStreams
}
```
<br>

Two Methods one for playing and one for stopping everything:

```java
public class Audio {
    private final HashMap<String, Clip> heavyClipCache = new HashMap<>(); // Cache for AudioClips
    private final ArrayList<BufferedInputStream> heavyBufferedInputStreamCache = new ArrayList<>(); // Cache for BufferedInputStreams
    private final ArrayList<AudioInputStream> heavyAudioInputStreamCache = new ArrayList<>(); // Cache for AudioInputStreams

    // Loads music files and plays them
    public void audioPlayer(String audioFilePath) {
        CompletableFuture.runAsync(() -> {
            try {
                if (heavyClipCache.get(audioFilePath) != null) {
                    Clip clip = heavyClipCache.get(audioFilePath);
                    clip.setFramePosition(0);
                    clip.start();
                    return;
                }

                ClassLoader classLoader = getClass().getClassLoader();
                InputStream audioFileInputStream;

                audioFileInputStream = Files.newInputStream(Paths.get(audioFilePath)); // Audio is local
                audioFileInputStream = classLoader.getResourceAsStream(audioFilePath); // Audio is in the JAR file

                // Check if the audio file was found
                if (audioFileInputStream == null) throw new IllegalArgumentException("The audio file was not found: " + audioFilePath);

                BufferedInputStream bufferedInputStream = new BufferedInputStream(audioFileInputStream);
                AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(bufferedInputStream);

                Clip clip = AudioSystem.getClip();
                clip.open(audioInputStream);

                // Load long audio files into the cache to free up resources
                if (clip.getMicrosecondLength() > 1000000) {
                    heavyBufferedInputStreamCache.add(bufferedInputStream);
                    heavyAudioInputStreamCache.add(audioInputStream);
                    heavyClipCache.put(audioFilePath, clip);
                }

                // Add a LineListener to release resources when playback is finished
                clip.addLineListener(event -> {
                    if (event.getType() == LineEvent.Type.STOP) {
                        try {
                            if (!heavyClipCache.containsKey(audioFilePath)) {
                                clip.close();
                                audioInputStream.close();
                                bufferedInputStream.close();
                            }
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                });

                clip.start();

            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    // Stops the music
    public void stopHeavyAudio() {
        CompletableFuture.runAsync(() -> {
            try {
                for (Clip clip : heavyClipCache.values()) clip.stop();
                for (AudioInputStream audioInputStream : heavyAudioInputStreamCache) audioInputStream.close();
                for (BufferedInputStream bufferedInputStream : heavyBufferedInputStreamCache) bufferedInputStream.close();

                heavyBufferedInputStreamCache.clear();
                heavyAudioInputStreamCache.clear();
                heavyClipCache.clear();

            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }
}
```
<br>

### Center Image

With that you can create a point to center the image, you need an image loader:
```java
public class CenterThings {
    public Point locatePoint(String image, int width, int height) {
        BufferedImage img = reader(image);
        return new Point((width -  img.getWidth()) / 2, (height - img.getHeight()) / 2);
    }
}
```
<br>

### Center JFrame

With that, you can center your JFrame:
```java
public class CenterThings {
    public Point centerFrame(JFrame frame) {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize(); // Screen Size
        int x = ((screenSize.width - frame.getWidth()) / 2);
        int y = ((screenSize.height - frame.getHeight()) / 2);
        return new Point(x, y);
    }
}
```
<br>

### Check connection to specified server

```java
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class CheckConnection {
    public boolean checkSQLConnection(String ip, String port) {
        CompletableFuture<Boolean> future = CompletableFuture.supplyAsync(() -> {
            try (Socket socket = new Socket()) {
                // Attempting to establish a connection to the server
                socket.connect(new InetSocketAddress(ip, Integer.parseInt(port)), 1000); // Timeout of 1 second
                return true;
            } catch (IOException e) {
                return false;
            }
        });

        try {
            return future.get(); // Waiting for the result of the asynchronous call
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }
}
```
<br>

### System Latency

```java
public class SystemLatency {
    private long startTime = System.currentTimeMillis(); // Start time
    
    public long calculateSystemLatency() {
        long currentTime = System.currentTimeMillis(); // Current time
        long latency = currentTime - startTime; // Latency
        startTime = currentTime;
        return latency;
    }
}
```
<br>

### Write a debug log file

```java
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;

public class DebugLogger {
    public void soutLogger(String file, String message) {
        CompletableFuture.runAsync(() -> { // Asynchronous call
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(file, true))) {
                writer.append(message);
                writer.newLine();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }
}
```
<br>

### Read from a json file

First of all, you need this maven dependency:
```xml
<dependencies>
    <dependency>
        <groupId>com.fasterxml.jackson.core</groupId>
        <artifactId>jackson-databind</artifactId>
        <version>2.15.2</version>   <!-- Use the newest version -->
        </dependency>
</dependencies>
```

Then you need these imports:
```java
// Dependencies
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

// Normal imports
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
```

Then you can use this method to create a JsonNode:
```java
public class JsonReader {
    public JsonNode readJson(String json) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            InputStream inputStream;
            if (json.endsWith(".json")) inputStream = Files.newInputStream(Paths.get(json)); // JSON is Local
            else inputStream = getClass().getResourceAsStream("config/" + json + ".json"); // JSON is in Jar
            if (inputStream == null) return null;
            return mapper.readTree(inputStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
```
<br>

### Foreground color changer

If you want to change the foreground color of a component, depending on the background color.<br>
First you have to calculate the average color of a part of the background.<br>
For this you need to define a rectangle
```java
public class AverageColor {
    public Color getAverageColorInRectangle(Rectangle rectangle, JPanel panel) {
        BufferedImage image = new BufferedImage(panel.getWidth(), panel.getHeight(), BufferedImage.TYPE_INT_RGB);
        panel.paint(image.getGraphics());

        int startX = rectangle.x;
        int startY = rectangle.y;
        int endX = rectangle.x + rectangle.width;
        int endY = rectangle.y + rectangle.height;

        int totalRed = 0;
        int totalGreen = 0;
        int totalBlue = 0;
        int pixelCount = 0;

        for (int x = startX; x < endX; x++) {
            for (int y = startY; y < endY; y++) {
                int pixel = image.getRGB(x, y);
                int red = (pixel >> 16) & 0xFF;
                int green = (pixel >> 8) & 0xFF;
                int blue = pixel & 0xFF;

                totalRed += red;
                totalGreen += green;
                totalBlue += blue;
                pixelCount++;
            }
        }

        int averageRed = totalRed / pixelCount;
        int averageGreen = totalGreen / pixelCount;
        int averageBlue = totalBlue / pixelCount;

        return new Color(averageRed, averageGreen, averageBlue);
    }
}
```
When you have the average color, you can use this method to determine the best foreground color:
```java
public class AverageColor {
    public Color calculateForegroundColor(Color color) {
        int r = color.getRed();
        int g = color.getGreen();
        int b = color.getBlue();

        if (r + g + b > 382) return Color.BLACK;
        else return Color.WHITE;
    }
}
```