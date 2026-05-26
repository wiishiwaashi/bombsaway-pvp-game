import java.awt.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.io.*;
import javax.imageio.ImageIO;

public class Bomb extends GameObject {

    private boolean isExploding;
    private BufferedImage bombImg;
    private BufferedImage explodingBombImg;
    private int ownerId;
    private long plantedTime;


    public Bomb(double x, double y, int ownerId) {
        super(x, y);
        isExploding = false;
        this.ownerId = ownerId;
        this.plantedTime = System.currentTimeMillis();

        try {
            bombImg = ImageIO.read(new File("BombImg.png"));
            explodingBombImg = ImageIO.read(new File("ExplodingBombImg.png"));
        } catch (Exception e) {
            System.out.println("Could not load bomb image");
        }
    }

    public void draw(Graphics2D g2d) {
        AffineTransform reset = g2d.getTransform();

        if (!isExploding) {
            g2d.drawImage(bombImg, (int) getXPosition(), (int) getYPosition(), 50, 50, null);
        } else {
            g2d.drawImage(explodingBombImg, (int) getXPosition() - 50, (int) getYPosition() - 50, 150, 150, null);
        }
        
        g2d.setTransform(reset);
    }

    public void explode(GameCanvas canvas, Player player) {
        isExploding = true;
        // draw the exploding bomb
    }

    public int getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(int id){
        ownerId = id;
    }

    public long getPlantedTime() {
        return plantedTime;
    }

    public void setPlantedTime(long time){
        plantedTime = time;
    }
}
