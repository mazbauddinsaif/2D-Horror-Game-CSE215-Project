package game.core;

import game.data.AudioPlayer;
import game.data.GameHistory;
import game.data.GameRecord;
import game.data.AudioPlayer.SoundType;
import game.entities.Ghost;
import game.entities.Player;
import game.objects.Door;
import game.objects.GameObject;
import game.objects.Item;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.text.SimpleDateFormat;
import java.util.Date;

public class GamePanel extends JPanel implements ActionListener {

    final int tileSize = 48;
    final int panelWidth = 15 * tileSize;
    final int panelHeight = 11 * tileSize;
    final int FPS = 60;

    // for scrolling
    private static final int HIS_LINE_H = 20; // Height of each history while printing it on screen
    private static final int HIS_SECTION_GAP_UP = 100; // distance of history section from top line
    private static final int HIS_SECTION_GAP_DOWN = 430; // distance of history section from back button
    private static final int HIS_GAP_HEIGHT = HIS_SECTION_GAP_DOWN - HIS_SECTION_GAP_UP;

    // 0 is for the road where character can move, 1 is wall, 4 is key, 5 = door
    final int[][] map = {
        {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1},
        {1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
        {1, 0, 1, 1, 1, 0, 1, 1, 1, 1, 1, 1, 0, 0, 1},
        {1, 0, 1, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 1},
        {1, 0, 1, 0, 1, 1, 1, 1, 1, 0, 1, 0, 1, 0, 1},
        {1, 0, 0, 0, 0, 0, 0, 4, 1, 0, 0, 0, 1, 0, 1},
        {1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 1, 1, 1, 0, 1},
        {1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
        {1, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 5, 1},
        {1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
        {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1}
    };

    // Object Declaration
    private Player player;
    private Ghost ghost;
    private Door exitDoor;
    private final AudioPlayer audioPlayer;
    private final GameHistory gameHistory;

    private final ArrayList<GameObject> items = new ArrayList<>();
    private Timer gameTimer; //built in method for time handle
    private long startTime = 0;
    private long endTime = 0;
    private long finalScore = 0;
    private GameState gameState = GameState.MENU;

    private int historyScrollStart = 0;

    // all button shape objects
    private final Rectangle startButton;
    private final Rectangle historyButton;
    private final Rectangle backButton;
    private final Rectangle restartButton;
    private final Rectangle menuButton;

    public GamePanel() {
        setPreferredSize(new Dimension(panelWidth, panelHeight));
        setBackground(Color.BLACK);
        setDoubleBuffered(true); //for stopping flickering while using painting()

        audioPlayer = new AudioPlayer();
        gameHistory = new GameHistory();

        addKeyListener(new GameKeyListener());
        addMouseListener(new MenuMouseListener());
        addMouseWheelListener(new ScrollHandler());
        setFocusable(true); //control if the keyboard is in focus or not

        int buttonWidth = 300;
        int buttonHeight = 50;
        int centerX = (panelWidth / 2) - (buttonWidth / 2);

        startButton = new Rectangle(centerX, (panelHeight / 2) - 40, buttonWidth, buttonHeight);
        historyButton = new Rectangle(centerX, (panelHeight / 2) + 40, buttonWidth, buttonHeight);

        int backButtonWidth = 400;
        int back_centerX = (panelWidth / 2) - (backButtonWidth / 2);
        backButton = new Rectangle(back_centerX, panelHeight - 60, backButtonWidth, buttonHeight);

        restartButton = new Rectangle(centerX, panelHeight / 2 + 130, buttonWidth, buttonHeight);
        menuButton = new Rectangle(centerX, panelHeight / 2 + 200, buttonWidth, buttonHeight);

        requestFocusInWindow(); //window te jokhon thakbo oi somoy keyboard kaj kore
    }

    /**
     * reset the map and create it again with coins , key , door, etc
     */
    private void initializeGameObjects() {
        // player is at (1, 1) index of map
        player = new Player(tileSize * 1, tileSize * 1, tileSize);
        // location of ghost 
        ghost = new Ghost(tileSize * 13, tileSize * 9, tileSize);

        items.clear();
        //setting key and coin on the map
        for (int y = 0; y < map.length; y++) { //map array row size
            for (int x = 0; x < map[0].length; x++) { //map array column size
                if (map[y][x] == 4) { // setting key
                    items.add(new Item(tileSize * x, tileSize * y, Item.Type.KEY, tileSize));
                } else if (map[y][x] == 0 && Math.random() < 0.05) { // setting random coins
                    items.add(new Item(tileSize * x, tileSize * y, Item.Type.COIN, tileSize));
                } else if (map[y][x] == 5) { // door
                    exitDoor = new Door(tileSize * x, tileSize * y, tileSize);
                }
            }
        }
    }

    public void startGameLoop() {
        gameTimer = new Timer(1000 / FPS, this); // (delay , ActionListener)
        // eta use korsi jate ekta fixed time por por ActionListener k notify kore..use korchi game smooth korar jnno..
    }

    /**
     * when we click on start this method works to go to playing mode
     */
    private void startGameTransition() {
        gameState = GameState.PLAYING;
        startTime = System.currentTimeMillis();
        initializeGameObjects();
        if (gameTimer != null && !gameTimer.isRunning()) {
            gameTimer.start();
        }
        audioPlayer.startBGM();
        repaint(); //inherits from JComponent and we are using it to smoothen th game while transition
    }

    /**
     * calculate final score and save the game history.
     */
    private void finalizeGame(String outcome) {
        endTime = (System.currentTimeMillis() - startTime) / 1000;
        // Score = Survival Time (1 point/sec) + Total Coin Points
        finalScore = endTime + player.score;
        int coinCount = player.score / Item.COIN_POINTS; // Convert points back to count for history

        GameRecord newRecord = new GameRecord(endTime, coinCount, finalScore, outcome, System.currentTimeMillis());
        gameHistory.saveHistory(newRecord);

        audioPlayer.stopBGM();
        gameTimer.stop();
        gameState = outcome.equals("Escaped") ? GameState.ESCAPED : GameState.GAME_OVER;
        repaint();
    }

    /**
     * Resets the game state and returns to the Main Menu.
     */
    private void goToMainMenu() {
        audioPlayer.playSound(SoundType.BUTTON_CLICK);
        gameState = GameState.MENU;
        repaint();
    }

    /**
     * Restarts the game by going back to the menu and starting a transition.
     */
    private void restartGame() {
        audioPlayer.playSound(SoundType.BUTTON_CLICK);
        startGameTransition();
    }

    /**
     * Actions
     */
    @Override
    public void actionPerformed(ActionEvent e) {

        if (!isFocusOwner()) { //focus on keyboard
            requestFocusInWindow();
        }

        if (gameState == GameState.PLAYING) {
            updateGame();
        }

        repaint();
    }

    /**
     * game logic during the PLAYING state.
     */
    private void updateGame() {
        //update entities
        player.update(map, tileSize);
        ghost.update(map, tileSize, player);

        //chk Interactions with Item and door
        checkInteractions();

        //chk Game Over
        if (player.getBounds().intersects(ghost.getBounds())) {
            finalizeGame("Caught");
            audioPlayer.playSound(SoundType.CAUGHT);
        }
    }

    /**
     * check for collisions between the player and other objects.
     */
    private void checkInteractions() {
        Iterator<GameObject> itemIterator = items.iterator();
        while (itemIterator.hasNext()) {
            Item item = (Item) itemIterator.next();
            if (player.getBounds().intersects(item.getBounds())) {
                player.collectItem(item);
                if (item.type == Item.Type.COIN) {
                    audioPlayer.playSound(SoundType.COIN);
                } else if (item.type == Item.Type.KEY) {
                    audioPlayer.playSound(SoundType.KEY);
                }
                itemIterator.remove();
            }
        }

        //chk door interaction
        exitDoor.canInteract = player.getBounds().intersects(exitDoor.getBounds());
    }

    // draw all objects
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;

        if (gameState == GameState.PLAYING || gameState == GameState.PAUSED) {
            drawMap(g2);

            for (GameObject item : items) {
                item.draw(g2);
            }

            exitDoor.draw(g2, player.hasKey);

            player.draw(g2);
            ghost.draw(g2);

            drawHUD(g2);
        }

        drawStateScreen(g2);
        g2.dispose();
    }

    private void drawMap(Graphics2D g2) {
        for (int y = 0; y < map.length; y++) {
            for (int x = 0; x < map[0].length; x++) {
                int xPos = x * tileSize;
                int yPos = y * tileSize;

                if (map[y][x] == 1) { // Wall
                    g2.setColor(Color.DARK_GRAY.darker());
                    g2.fillRect(xPos, yPos, tileSize, tileSize);
                    g2.setColor(Color.BLACK);
                    g2.drawRect(xPos, yPos, tileSize, tileSize);
                } else { // Floor
                    g2.setColor(new Color(30, 30, 30));
                    g2.fillRect(xPos, yPos, tileSize, tileSize);
                }
            }
        }
    }

    private void drawHUD(Graphics2D g2) {
        g2.setFont(new Font("Monospaced", Font.BOLD, 18));
        g2.setColor(Color.WHITE);

        long elapsedSeconds = (System.currentTimeMillis() - startTime) / 1000;
        String timeStr = String.format("%02d:%02d", elapsedSeconds / 60, elapsedSeconds % 60);
        g2.drawString("Time: " + timeStr, 10, 20);

        int coinCount = player.score / Item.COIN_POINTS;
        g2.drawString("Coins: " + coinCount, 10, 40);

        String keyStatus = player.hasKey ? "Key: Collected" : "Key: Missing";
        g2.drawString(keyStatus, 10, 60);
    }

    private void drawStateScreen(Graphics2D g2) {
        Color overlayColor = new Color(0, 0, 0, 200);

        switch (gameState) {
            case MENU:
                drawMenuScreen(g2, overlayColor);
                break;
            case PAUSED:
                drawPauseScreen(g2, overlayColor);
                break;
            case GAME_OVER:
                drawGameOverScreen(g2);
                break;
            case ESCAPED:
                drawEscapedScreen(g2);
                break;
            case HISTORY:
                drawHistoryScreen(g2, overlayColor);
                break;
        }
    }

    // drawing screens
    private void drawMenuScreen(Graphics2D g2, Color overlayColor) {
        String message = "ESCAPE FROM THE GHOST";
        g2.setColor(overlayColor);
        g2.fillRect(0, 0, panelWidth, panelHeight);

        g2.setFont(new Font("Serif", Font.BOLD, 48));
        g2.setColor(Color.RED.darker());
        g2.drawString(message, panelWidth / 2 - g2.getFontMetrics().stringWidth(message) / 2, panelHeight / 3);

        // Start Button
        g2.setColor(Color.GREEN.darker());
        g2.fill(startButton);
        g2.setColor(Color.WHITE);
        g2.draw(startButton);
        String startText = "START GAME (ENTER)";
        g2.setFont(new Font("Serif", Font.BOLD, 20));
        FontMetrics fmStart = g2.getFontMetrics();
        g2.drawString(startText,
                startButton.x + (startButton.width - fmStart.stringWidth(startText)) / 2,
                startButton.y + (startButton.height / 2) + (fmStart.getAscent() / 2) - 2);

        // History Button
        g2.setColor(Color.BLUE.darker());
        g2.fill(historyButton);
        g2.setColor(Color.WHITE);
        g2.draw(historyButton);
        String historyText = "HISTORY (H)";
        g2.setFont(new Font("Serif", Font.BOLD, 20)); // Reset font after button draw
        FontMetrics fmHistory = g2.getFontMetrics();
        g2.drawString(historyText,
                historyButton.x + (historyButton.width - fmHistory.stringWidth(historyText)) / 2,
                historyButton.y + (historyButton.height / 2) + (fmHistory.getAscent() / 2) - 2);

        g2.setFont(new Font("Serif", Font.PLAIN, 18));
        g2.setColor(Color.WHITE);
        String instruction = "After starting game , "
                + "Use WASD or Arrows to move. P to pause.";
        g2.drawString(instruction,
                panelWidth / 2 - g2.getFontMetrics().stringWidth(instruction) / 2, panelHeight / 2 + 140);
    }

    private void drawPauseScreen(Graphics2D g2, Color overlayColor) {
        String message = "Game is PAUSED";
        String instruction = "Press P to Resume";
        g2.setColor(overlayColor);
        g2.fillRect(0, 0, panelWidth, panelHeight);

        g2.setFont(new Font("Serif", Font.BOLD, 48));
        g2.setColor(Color.WHITE);
        g2.drawString(message, panelWidth / 2 - g2.getFontMetrics().stringWidth(message) / 2, panelHeight / 2 - 30);

        g2.setFont(new Font("Serif", Font.PLAIN, 24));
        g2.drawString(instruction, panelWidth / 2 - g2.getFontMetrics().stringWidth(instruction) / 2, panelHeight / 2 + 30);
    }

    private void drawGameOverScreen(Graphics2D g2) {
        String message = "YOU FAILED TO ESCAPE...";
        long finalTime = endTime;
        int finalCoinsCount = (int) (player.score / Item.COIN_POINTS);
        long FinalScore = finalScore;

        g2.setColor(new Color(150, 0, 0, 220));
        g2.fillRect(0, 0, panelWidth, panelHeight);

        g2.setFont(new Font("Serif", Font.BOLD, 48));
        g2.setColor(Color.RED.brighter());
        g2.drawString(message, panelWidth / 2 - g2.getFontMetrics().stringWidth(message) / 2, panelHeight / 3);

        g2.setFont(new Font("Serif", Font.PLAIN, 24));
        g2.setColor(Color.WHITE);
        g2.drawString("Coins Collected: " + finalCoinsCount, panelWidth / 2 - g2.getFontMetrics().stringWidth("Coins Collected: " + finalCoinsCount) / 2, panelHeight / 2);
        g2.drawString("Survival Time: " + finalTime + " seconds", panelWidth / 2 - g2.getFontMetrics().stringWidth("Survival Time: " + finalTime + " seconds") / 2, panelHeight / 2 + 40);
        g2.drawString("TOTAL SCORE: " + FinalScore, panelWidth / 2 - g2.getFontMetrics().stringWidth("TOTAL SCORE: " + FinalScore) / 2, panelHeight / 2 + 80);

        g2.setFont(new Font("Serif", Font.BOLD, 20));

        // draw restart button green
        g2.setColor(Color.GREEN.darker());
        g2.fill(restartButton);
        g2.setColor(Color.WHITE);
        g2.draw(restartButton);
        String restartText = "RESTART (R)";
        FontMetrics fmRestart = g2.getFontMetrics();
        g2.drawString(restartText,
                restartButton.x + (restartButton.width - fmRestart.stringWidth(restartText)) / 2,
                restartButton.y + (restartButton.height / 2) + (fmRestart.getAscent() / 2) - 2);

        // draw menu button blue
        g2.setColor(Color.BLUE.darker());
        g2.fill(menuButton);
        g2.setColor(Color.WHITE);
        g2.draw(menuButton);
        String menuText = "MAIN MENU (ESC)";
        FontMetrics fmMenu = g2.getFontMetrics();
        g2.drawString(menuText,
                menuButton.x + (menuButton.width - fmMenu.stringWidth(menuText)) / 2,
                menuButton.y + (menuButton.height / 2) + (fmMenu.getAscent() / 2) - 2);
    }

    private void drawEscapedScreen(Graphics2D g2) {
        String message = "YOU ESCAPED!";
        long escapeTime = endTime;
        int finalCoinsCount = (int) (player.score / Item.COIN_POINTS);
        long finalScoreEscaped = finalScore;

        g2.setColor(new Color(0, 100, 0, 220));
        g2.fillRect(0, 0, panelWidth, panelHeight);

        g2.setFont(new Font("Serif", Font.BOLD, 48));
        g2.setColor(Color.GREEN.brighter());
        g2.drawString(message, panelWidth / 2 - g2.getFontMetrics().stringWidth(message) / 2, panelHeight / 3);

        g2.setFont(new Font("Serif", Font.PLAIN, 24));
        g2.setColor(Color.WHITE);
        g2.drawString("Coins Collected: " + finalCoinsCount, panelWidth / 2 - g2.getFontMetrics().stringWidth("Coins Collected: " + finalCoinsCount) / 2, panelHeight / 2 - 20);
        g2.drawString("Time Points: " + escapeTime, panelWidth / 2 - g2.getFontMetrics().stringWidth("Time Points: " + escapeTime) / 2, panelHeight / 2 + 20);
        g2.drawString("TOTAL SCORE: " + finalScoreEscaped, panelWidth / 2 - g2.getFontMetrics().stringWidth("TOTAL SCORE: " + finalScoreEscaped) / 2, panelHeight / 2 + 60);

        g2.setFont(new Font("Serif", Font.BOLD, 20));

        // restart button draw green
        g2.setColor(Color.GREEN.darker());
        g2.fill(restartButton);
        g2.setColor(Color.WHITE);
        g2.draw(restartButton);
        String restartText = "RESTART (R)";
        FontMetrics fmRestart = g2.getFontMetrics();
        g2.drawString(restartText,
                restartButton.x + (restartButton.width - fmRestart.stringWidth(restartText)) / 2,
                restartButton.y + (restartButton.height / 2) + (fmRestart.getAscent() / 2) - 2);

        // draw menu button blue
        g2.setColor(Color.BLUE.darker());
        g2.fill(menuButton);
        g2.setColor(Color.WHITE);
        g2.draw(menuButton);
        String menuText = "MAIN MENU (ESC)";
        FontMetrics fmMenu = g2.getFontMetrics();
        g2.drawString(menuText,
                menuButton.x + (menuButton.width - fmMenu.stringWidth(menuText)) / 2,
                menuButton.y + (menuButton.height / 2) + (fmMenu.getAscent() / 2) - 2);
    }

    private void drawHistoryScreen(Graphics2D g2, Color overlayColor) {
        String message = "GAME HISTORY";
        List<GameRecord> historyList = gameHistory.getHistoryList();

        g2.setColor(overlayColor);
        g2.fillRect(0, 0, panelWidth, panelHeight);

        g2.setFont(new Font("Serif", Font.BOLD, 48));
        g2.setColor(Color.WHITE);
        g2.drawString(message, panelWidth / 2 - g2.getFontMetrics().stringWidth(message) / 2, 50);

        g2.setFont(new Font("Monospaced", Font.PLAIN, 14));
        g2.setColor(Color.LIGHT_GRAY);

        int headerY = 90; // Y position for the header line
        if (historyList.isEmpty()) {
            String noRecords = "No previous games recorded.";
            g2.drawString(noRecords, panelWidth / 2 - g2.getFontMetrics().stringWidth(noRecords) / 2, headerY);
        } else {
            // Table Header
            String header = String.format("%-20s | %-7s | %-12s | %-5s | %-5s", "DATE", "OUTCOME", "TIME (s)", "COINS", "SCORE");
            g2.drawString(header, 20, headerY);
            g2.drawLine(20, headerY + 5, panelWidth - 20, headerY + 5);

            final int totalRecords = historyList.size();

            // maximum scroll limit
            int contentHeight = totalRecords * HIS_LINE_H;
            int maxScrollOffset = contentHeight - HIS_GAP_HEIGHT;

            //  scroll start (prevents scrolling too far up/down)
            historyScrollStart = Math.min(historyScrollStart, maxScrollOffset);
            historyScrollStart = Math.max(historyScrollStart, 0);

            // visible window for scrolling
            Shape originalClip = g2.getClip();
            Rectangle scrollClipArea = new Rectangle(20, HIS_SECTION_GAP_UP, panelWidth - 40, HIS_GAP_HEIGHT);
            g2.setClip(scrollClipArea);

            // show history records 
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            int recordDrawY = HIS_SECTION_GAP_UP + HIS_LINE_H;

            for (int i = 0; i < totalRecords; i++) {
                GameRecord record = historyList.get(i);
                String dateStr = sdf.format(new Date(record.timestamp));

                //  drawing Y 
                int drawY = recordDrawY + (i * HIS_LINE_H) - historyScrollStart;

                String recordStr = String.format("%-20s | %-7s | %-12d | %-5d | %d",
                        dateStr, record.outcome, record.survivalTimeSeconds, record.coinsCollected, record.finalScore);
                g2.drawString(recordStr, 20, drawY);
            }

            //  restore the original visible region of scrolling
            g2.setClip(originalClip);

        }

        // back Button draw  
        g2.setColor(Color.BLUE.darker().darker());
        g2.fill(backButton);
        g2.setColor(Color.WHITE);
        g2.draw(backButton);
        String backText = "BACK TO MENU (ESC/Backspace)";
        g2.setFont(new Font("Serif", Font.BOLD, 20));
        FontMetrics fmBack = g2.getFontMetrics();
        g2.drawString(backText,
                backButton.x + (backButton.width - fmBack.stringWidth(backText)) / 2,
                backButton.y + (backButton.height / 2) + (fmBack.getAscent() / 2) - 2);
    }

    //Handling Action Input
    private class GameKeyListener extends KeyAdapter {

        @Override
        public void keyPressed(KeyEvent e) {
            int key = e.getKeyCode();

            if (gameState == GameState.MENU) {
                if (key == KeyEvent.VK_ENTER) {
                    audioPlayer.playSound(SoundType.BUTTON_CLICK);
                    startGameTransition();
                } else if (key == KeyEvent.VK_H) {
                    audioPlayer.playSound(SoundType.BUTTON_CLICK);
                    gameHistory.loadHistory();
                    gameState = GameState.HISTORY;
                    historyScrollStart = 0;
                    repaint();
                }
            } else if (gameState == GameState.HISTORY) {
                if (key == KeyEvent.VK_BACK_SPACE || key == KeyEvent.VK_ESCAPE) {
                    audioPlayer.playSound(SoundType.BUTTON_CLICK);
                    gameState = GameState.MENU;
                    repaint();
                }
            } else if (gameState == GameState.PLAYING && key == KeyEvent.VK_P) {
                gameState = GameState.PAUSED;
                audioPlayer.stopBGM(); // Pause music when pausing
            } else if (gameState == GameState.PAUSED && key == KeyEvent.VK_P) {
                gameState = GameState.PLAYING;
                audioPlayer.startBGM(); // again start music when resuming
            } else if (gameState == GameState.GAME_OVER || gameState == GameState.ESCAPED) {
                if (key == KeyEvent.VK_R) {
                    restartGame();
                } else if (key == KeyEvent.VK_ESCAPE) {
                    goToMainMenu();
                }
            }

            if (gameState == GameState.PLAYING) {
                player.keyPressed(key);

                // door escape chk
                if (key == KeyEvent.VK_ENTER && exitDoor.canInteract) {
                    if (player.hasKey) {
                        finalizeGame("Escaped");
                        audioPlayer.playSound(SoundType.KEY);
                    } else {
                        System.out.println("ALERT: The door is locked! You need the key.");
                        audioPlayer.playSound(SoundType.LOCKED);
                    }
                }
            }
        }

        @Override
        public void keyReleased(KeyEvent e) {
            if (gameState == GameState.PLAYING) {
                player.keyReleased(e.getKeyCode());
            }
        }
    }

    private class MenuMouseListener extends MouseAdapter {

        @Override
        public void mousePressed(MouseEvent e) {
            Point p = e.getPoint();

            if (gameState == GameState.MENU) {
                if (startButton.contains(p)) {
                    audioPlayer.playSound(SoundType.BUTTON_CLICK);
                    startGameTransition();
                } else if (historyButton.contains(p)) {
                    audioPlayer.playSound(SoundType.BUTTON_CLICK);
                    gameHistory.loadHistory();
                    gameState = GameState.HISTORY;
                    historyScrollStart = 0;
                    repaint();
                }
            } else if (gameState == GameState.HISTORY) {
                if (backButton.contains(p)) {
                    audioPlayer.playSound(SoundType.BUTTON_CLICK);
                    gameState = GameState.MENU;
                    repaint();
                }
            } else if (gameState == GameState.GAME_OVER || gameState == GameState.ESCAPED) {
                if (restartButton.contains(p)) {
                    restartGame();
                } else if (menuButton.contains(p)) {
                    goToMainMenu();
                }
            }
        }
    }

    private class ScrollHandler implements MouseWheelListener {

        @Override
        public void mouseWheelMoved(MouseWheelEvent e) {
            if (gameState == GameState.HISTORY) {
                int scrollAmount = e.getUnitsToScroll() * HIS_LINE_H;

                // Adjust the scroll start
                historyScrollStart += scrollAmount;

                repaint();
            }
        }
    }
}
