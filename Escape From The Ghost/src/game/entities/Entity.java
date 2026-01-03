package game.entities;

import game.objects.GameObject;

public abstract class Entity extends GameObject {
    protected int speed = 3;
    protected int dx = 0, dy = 0; // displacement 

    public Entity(int x, int y, int tileSize, int speed) {
        super(x, y, tileSize);
        this.speed = speed;
        // making entities slightly smaller than the tile for better movement 
        this.width = tileSize - 10;
        this.height = tileSize - 10;
        this.x += 5; // Center entity in tile
        this.y += 5; // Center entity in tile
    }

    public void move(final int[][] map, final int tileSize) {
        int nextX = x + dx;
        int nextY = y + dy;

        //  chk for X-axis collision
        if (!isColliding(nextX, y, map, tileSize)) {
            x = nextX;
        }
        // chk for Y-axis collision
        if (!isColliding(x, nextY, map, tileSize)) {
            y = nextY;
        }
    }

    protected boolean isColliding(int nextX, int nextY, final int[][] map, final int tileSize) {
        // chk the four corners of the entity for collision chk
        int col1 = nextX / tileSize;
        int row1 = nextY / tileSize;

        int col2 = (nextX + width - 1) / tileSize;
        int row2 = nextY / tileSize;

        int col3 = nextX / tileSize;
        int row3 = (nextY + height - 1) / tileSize;

        int col4 = (nextX + width - 1) / tileSize;
        int row4 = (nextY + height - 1) / tileSize;

        return map[row1][col1] == 1 || map[row2][col2] == 1 ||
               map[row3][col3] == 1 || map[row4][col4] == 1;
    }

    public abstract void update(int[][] map, int tileSize);
}