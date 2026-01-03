package game.entities;

import java.awt.*;

public class Ghost extends Entity {

    public Ghost(int x, int y, int tileSize) {
        super(x, y, tileSize, 2); // Ghost speed is slower than char
        this.width = tileSize; // Ghost full tiles e ache
        this.height = tileSize;
        this.x -= 5; // adjuting position back to tile corner
        this.y -= 5;
    }

    @Override
    protected boolean isColliding(int nextX, int nextY, final int[][] map, final int tileSize) {
        return false;
    }

    public void update(int[][] map, int tileSize, Player player) {
        int targetX = player.x + player.width / 2;
        int targetY = player.y + player.height / 2;

        int monsterCenterX = this.x + this.width / 2;
        int monsterCenterY = this.y + this.height / 2;

        // calculate direction 
        int deltaX = targetX - monsterCenterX;
        int deltaY = targetY - monsterCenterY;

        double distance = Math.sqrt(deltaX * deltaX + deltaY * deltaY);

        dx = 0;
        dy = 0;

        if (distance > width / 2) { // plyr er tiles e na thakle tokhon kaj krbe
            dx = (int) (speed * (deltaX / distance));
            dy = (int) (speed * (deltaY / distance));
        }

        move(map, tileSize);
    }

    @Override
    public void update(int[][] map, int tileSize) { }

    @Override
    public void draw(Graphics2D g2) {
        g2.setColor(new Color(255, 255, 255, 180));
        g2.fillOval(x, y, width, height);
        g2.setColor(Color.RED);
        g2.drawOval(x, y, width, height);

        g2.fillOval(x + width/4, y + height/4, width/8, height/8);
        g2.fillOval(x + width/4 * 3 - width/8, y + height/4, width/8, height/8);
    }
}