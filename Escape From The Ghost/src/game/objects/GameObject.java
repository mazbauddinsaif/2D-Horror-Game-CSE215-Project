package game.objects;

import java.awt.*;

public abstract class GameObject {

    public int x;
    public int y;
    public int width;
    public int height;

    public GameObject(int x, int y, int tileSize) {
        this.x = x;
        this.y = y;
        this.width = tileSize;
        this.height = tileSize;
    }

    /**
     * Gets the rectangular bounding box for collision detection.
     *
     * @return A Rectangle object representing the bounds.
     */
    public Rectangle getBounds() {
        return new Rectangle(x, y, width, height);
    }

    /**
     * Abstract method for drawing the object on the screen.
     *
     * @param g2 The Graphics2D context.
     */
    public abstract void draw(Graphics2D g2);

    // Getters
//    public int getX() {
//        return x;
//    }
//
//    public int getY() {
//        return y;
//    }
}
