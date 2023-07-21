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
