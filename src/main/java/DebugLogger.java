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