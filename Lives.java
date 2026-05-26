
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.*;
import javax.imageio.ImageIO;


public class Lives extends GameObject{

    private BufferedImage heart;
    
    public Lives(double x, double y) {
        super(x, y);
        try { //https://www.geeksforgeeks.org/java/image-processing-in-java-read-and-write/
            heart = ImageIO.read(new File("HeartImg.png"));
        } catch (Exception e) {
            System.out.println("Could not load heart image");
        }
    }

    public void draw(Graphics2D g2d) {
        g2d.drawImage(heart, (int) getXPosition(), (int) getYPosition(), 50, 50, null);
    }

}
