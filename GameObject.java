import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;

abstract class GameObject {
    private double xPosition;
    private double yPosition;


    public GameObject(double x, double y) {
        xPosition = x;
        yPosition = y;
    }

    public abstract void draw(Graphics2D g2d);

    public double getXPosition() {return xPosition;}

    public double getYPosition() {return yPosition;}


    public void setXPosition(double x) {xPosition = x;}

    public void setYPosition(double y) {yPosition = y;}


    public Rectangle2D.Double getHitbox() {
        Rectangle2D.Double hitbox = new Rectangle2D.Double(getXPosition(), getYPosition(), 50, 50);
        return hitbox;
    }
}