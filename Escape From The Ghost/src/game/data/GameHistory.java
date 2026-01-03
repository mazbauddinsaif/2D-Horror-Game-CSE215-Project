package game.data;

import java.io.*;
import java.util.*;

public class GameHistory {
    private final String HISTORY_FILE = "game_history.txt";
    private final List<GameRecord> historyList = new ArrayList<>();

    public GameHistory() {
        loadHistory();
    }

    public void loadHistory() {
        historyList.clear();
        try (BufferedReader br = new BufferedReader(new FileReader(HISTORY_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 5) {
                    try {
                        String outcome = parts[0];
                        long time = Long.parseLong(parts[1]);
                        int coins = Integer.parseInt(parts[2]);
                        long score = Long.parseLong(parts[3]);
                        long timestamp = Long.parseLong(parts[4]);
                        historyList.add(new GameRecord(time, coins, score, outcome, timestamp));
                    } catch (NumberFormatException ex) {
                        System.err.println("Error parsing history line: " + line);
                    }
                }
            }
           

        } catch (FileNotFoundException e) {
            System.out.println("History file not found. Starting fresh.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Appends a new game record to the history file and reloads the in-memory list.
     * @param record The GameRecord to save.
     */
    public void saveHistory(GameRecord record) {
        try (FileWriter fw = new FileWriter(HISTORY_FILE, true)) {
            fw.write(record.toFileLine() + System.lineSeparator());
        } catch (IOException e) {
            e.printStackTrace();
        }
        // Always reload
        loadHistory();
    }

    /**
     * Gets the current sorted list of game records.
     * @return The list of records.
     */
    public List<GameRecord> getHistoryList() {
        return historyList;
    }
}