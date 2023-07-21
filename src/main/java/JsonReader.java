import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

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