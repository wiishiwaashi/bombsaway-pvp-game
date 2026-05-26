import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import javax.imageio.ImageIO;

public class Wall extends GameObject {
    private BufferedImage wall;

    public Wall(int x, int y) {
        super(x,y);

        try {
            wall = ImageIO.read(new File("WallImg.png"));
        } catch (Exception e) {
            System.out.println("Could not load wall image");
        }

    }

    /**
     * This method draws the wall.
     */
    public void draw(Graphics2D g2d) {
        g2d.drawImage(wall, (int) getXPosition(), (int) getYPosition(), 50, 50, null);
    }
}

