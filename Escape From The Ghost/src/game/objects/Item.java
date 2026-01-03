package game.objects;

import java.awt.*;

public class Item extends GameObject {
    public enum Type { KEY, COIN }
    public final Type type;

    public static final int COIN_POINTS = 5;

    public Item(int x, int y, Type type, int tileSize) {
        super(x + tileSize / 4, y + tileSize / 4, tileSize);
        this.type = type;
        this.width = tileSize / 2;
        this.height = tileSize / 2;
    }

    @Override
    public void draw(Graphics2D g2) {
        if (type == Type.KEY) {
            g2.setColor(Color.YELLOW);
            g2.fillRect(x, y, width, height); //square key
            g2.setColor(Color.BLACK);
            g2.setFont(new Font("Arial", Font.BOLD, 12));
            g2.drawString("K", x + 5, y + 15);
        } else if (type == Type.COIN) {
            g2.setColor(Color.ORANGE);
            g2.fillOval(x, y, width, height);
            g2.setColor(Color.BLACK);
            g2.setFont(new Font("Arial", Font.BOLD, 12));
            g2.drawString("$", x + 5, y + 15);
        }
    }
}