package game.data;

public class GameRecord {
    public final long survivalTimeSeconds;
    public final int coinsCollected; 
    public final long finalScore;
    public final String outcome; // "Escaped" or "Caught"
    public final long timestamp; // how much time a match played

    public GameRecord(long time, int coins, long score, String outcome, long timestamp) {
        this.survivalTimeSeconds = time;
        this.coinsCollected = coins;
        this.finalScore = score;
        this.outcome = outcome;
        this.timestamp = timestamp;
    }

    public String toFileLine() {
        return String.format("%s,%d,%d,%d,%d", outcome, survivalTimeSeconds, coinsCollected, finalScore, timestamp);
    }
}