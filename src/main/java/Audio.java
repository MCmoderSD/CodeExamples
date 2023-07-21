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