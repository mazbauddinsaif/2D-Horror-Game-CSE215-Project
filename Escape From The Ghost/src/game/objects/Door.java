package game.objects;

import java.awt.*;

public class Door extends GameObject {
    private boolean isLocked = true;
    public boolean canInteract = false;

    public Door(int x, int y, int tileSize) {
        super(x, y, tileSize);
    }

    @Override
    public void draw(Graphics2D g2) {
        draw(g2, false);
    }

    public void draw(Graphics2D g2, boolean hasKey) {
        isLocked = !hasKey;

        if (isLocked) {
            g2.setColor(Color.RED.darker());
            g2.fillRect(x, y, width, height);
            g2.setColor(Color.BLACK);
            g2.drawRect(x, y, width, height);
            g2.setColor(Color.YELLOW);
            g2.fillOval(x + width/2 - 5, y + height/2 - 5, 10, 10); 
        } else {
            g2.setColor(Color.GREEN.darker().darker()); 
            g2.fillRect(x, y, width, height);
            g2.setColor(Color.WHITE);
            g2.drawString("EXIT", x + width/4, y + height/2);
        }

        //  interaction 
        if (canInteract) {
            g2.setColor(Color.CYAN);
            g2.setFont(new Font("Monospaced", Font.BOLD, 10));
            String prompt = isLocked ? "Press ENTER (LOCKED)" : "Press ENTER to Escape";
            g2.drawString(prompt, x, y - 5);
        }
    }
}