public class SystemLatency {
    private long startTime = System.currentTimeMillis(); // Start time
    public long calculateSystemLatency() {
        long currentTime = System.currentTimeMillis(); // Current time
        long latency = currentTime - startTime; // Latency
        startTime = currentTime;
        return latency;
    }
}