package game.main;

import game.core.GamePanel;

import javax.swing.*;

public class HorrorGame extends JFrame {

    public static void main(String[] args) {

        HorrorGame frame = new HorrorGame();
        frame.setTitle("Escape From The Ghost");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false);

         GamePanel gamePanel = new GamePanel();
         frame.add(gamePanel);
        
        frame.pack(); // Ensure the size according to the internal components
        frame.setLocationRelativeTo(null); 
        frame.setVisible(true);

        gamePanel.startGameLoop();
    }
}
