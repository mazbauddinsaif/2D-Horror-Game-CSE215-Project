package game.entities;

import game.objects.Item;
import java.awt.*;
import java.awt.event.KeyEvent;

public class Player extends Entity {

    private boolean up, down, left, right = false;
    public int score = 0; 
    public boolean hasKey = false;

    public Player(int x, int y, int tileSize) {
        super(x, y, tileSize, 3); // Player speed is 3
        this.width = tileSize - 10;
        this.height = tileSize - 10;
        this.x += 5; // center entity in tile
        this.y += 5; // center entity in tile
    }

    @Override
    public void update(int[][] map, int tileSize) {
        dx = 0;
        dy = 0;

        if (up) {
            dy -= speed;
        }
        if (down) {
            dy += speed;
        }
        if (left) {
            dx -= speed;
        }
        if (right) {
            dx += speed;
        }
        if (up && down) {
            dy = 0;
        }
        if (left && right) {
            dx = 0;
        }


        move(map, tileSize);
    }

    @Override
    public void draw(Graphics2D g2) {
        g2.setColor(Color.BLUE);
        g2.fillOval(x, y, width, height);
        g2.setColor(Color.WHITE);
        g2.drawOval(x, y, width, height);
        // Draw a red dot for orientation
        g2.setColor(Color.RED);
        g2.fillOval(x + width / 2 - 2, y + height / 2 - 2, 4, 4);
    }

    public void keyPressed(int key) {
        if (key == KeyEvent.VK_W || key == KeyEvent.VK_UP) {
            up = true;
        }
        if (key == KeyEvent.VK_S || key == KeyEvent.VK_DOWN) {
            down = true;
        }
        if (key == KeyEvent.VK_A || key == KeyEvent.VK_LEFT) {
            left = true;
        }
        if (key == KeyEvent.VK_D || key == KeyEvent.VK_RIGHT) {
            right = true;
        }
    }

    public void keyReleased(int key) {
        if (key == KeyEvent.VK_W || key == KeyEvent.VK_UP) {
            up = false;
        }
        if (key == KeyEvent.VK_S || key == KeyEvent.VK_DOWN) {
            down = false;
        }
        if (key == KeyEvent.VK_A || key == KeyEvent.VK_LEFT) {
            left = false;
        }
        if (key == KeyEvent.VK_D || key == KeyEvent.VK_RIGHT) {
            right = false;
        }
    }

    public void collectItem(Item item) {
        if (item.type == Item.Type.COIN) {
            score += Item.COIN_POINTS;
        } else if (item.type == Item.Type.KEY) {
            hasKey = true;
        }
    }
}
