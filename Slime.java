import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import javax.imageio.ImageIO;

public class Slime extends GameObject {
    private int slimeID;
    private boolean isAlive;
    private boolean isRespawning;
    private boolean isMovingDown;
    private BufferedImage slime;
    private BufferedImage deadSlime;

    public Slime(double x, double y, int id) {
        super(x,y); // https://youtu.be/Qb_NUn0TSAU?si=aC3PFN901dWfx2WK
        slimeID = id;
        isAlive = true;
        isMovingDown = false;
        isMovingDown = true;

        try {
            slime = ImageIO.read(new File("SlimeImg.png"));
            deadSlime = ImageIO.read(new File("DeadSlimeImg.png"));
        } catch (Exception e) {
            System.out.println("Could not load slime or deadSlime image");
        }
    }

    public void draw(Graphics2D g2d) {
        if (this.isAlive) {
            g2d.drawImage(slime, (int) getXPosition(), (int) getYPosition(), 50, 50, null);
        } else {
            g2d.drawImage(deadSlime, (int) getXPosition(), (int) getYPosition(), 50, 50, null);
        }
        
    }

    public boolean checkIfAlive() {
        return isAlive;
    }

    public void die() {
        isAlive = false;
    }

    public void revive() {
        isAlive = true;
    }
}
