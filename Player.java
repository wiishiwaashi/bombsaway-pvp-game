import java.awt.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

public class Player extends GameObject {
    private int score;
    private int lives;
    private int playerID;
    private String name;

    private boolean canPlaceBomb;

    private BufferedImage p1MoveUp;
    private BufferedImage p1MoveDown;
    private BufferedImage p1MoveLeft;
    private BufferedImage p1MoveRight;

    private BufferedImage p2MoveUp;
    private BufferedImage p2MoveDown;
    private BufferedImage p2MoveLeft;
    private BufferedImage p2MoveRight;

    private boolean movingUp;
    private boolean movingDown;
    private boolean movingLeft;
    private boolean movingRight;

    public Player(int x, int y, int i, GameFrame gf){
        super(x, y); // https://youtu.be/Qb_NUn0TSAU?si=aC3PFN901dWfx2WK
        score = 0;
        lives = 5;
        playerID = i;
        canPlaceBomb = true;
        movingUp = false;
        movingDown = true;
        movingLeft = false;
        movingRight = false;

        try {
            p1MoveUp = ImageIO.read(new File("p1UpImg.png"));
            p1MoveDown = ImageIO.read(new File("p1DownImg.png"));
            p1MoveLeft = ImageIO.read(new File("p1LeftImg.png"));
            p1MoveRight = ImageIO.read(new File("p1RightImg.png"));
        } catch (Exception e) {
            System.out.println("Could not load Player  image");
        }

        try {
            p2MoveUp = ImageIO.read(new File("p2UpImg.png"));
            p2MoveDown = ImageIO.read(new File("p2DownImg.png"));
            p2MoveLeft = ImageIO.read(new File("p2LeftImg.png"));
            p2MoveRight = ImageIO.read(new File("p2RightImg.png"));
        } catch (Exception e) {
            System.out.println("Could not load Player 2 image");
        }

    }

    public void draw(Graphics2D g2d) {
        if (playerID==1) {
            if (movingUp) {
                g2d.drawImage(p1MoveUp, (int) getXPosition(), (int) getYPosition(), 50, 50, null);
            } else if (movingDown) {
                g2d.drawImage(p1MoveDown, (int) getXPosition(), (int) getYPosition(), 50, 50, null);
            } else if (movingLeft) {
                g2d.drawImage(p1MoveLeft, (int) getXPosition(), (int) getYPosition(), 50, 50, null);
            } else if (movingRight) {
                g2d.drawImage(p1MoveRight, (int) getXPosition(), (int) getYPosition(), 50, 50, null);
            }
        } else if (playerID==2) {
            if (movingUp) {
                g2d.drawImage(p2MoveUp, (int) getXPosition(), (int) getYPosition(), 50, 50, null);
            } else if (movingDown) {
                g2d.drawImage(p2MoveDown, (int) getXPosition(), (int) getYPosition(), 50, 50, null);
            } else if (movingLeft) {
                g2d.drawImage(p2MoveLeft, (int) getXPosition(), (int) getYPosition(), 50, 50, null);
            } else if (movingRight) {
                g2d.drawImage(p2MoveRight, (int) getXPosition(), (int) getYPosition(), 50, 50, null);
            }
        }
    }

    public void moveUp(GameCanvas canvas) {
        Rectangle2D.Double nextPosition = new Rectangle2D.Double(getXPosition(), getYPosition() - 5, 50, 50);

        for (GameObject o: canvas.getGameObjectList()) {
            if (o instanceof Wall) {
                if (nextPosition.intersects(o.getHitbox())) return;
            }
        }
        setYPosition(getYPosition() - 5);

        movingUp = true;
        movingDown = false;
        movingLeft = false;
        movingRight = false;
    }


    public void moveDown(GameCanvas canvas) { 
        Rectangle2D.Double nextPosition = new Rectangle2D.Double(getXPosition(), getYPosition() + 5, 50, 50);
        for (GameObject o: canvas.getGameObjectList()) {
            if (o instanceof Wall) { 
                if (nextPosition.intersects(o.getHitbox())) return;
            }
        }
        setYPosition(getYPosition() + 5);

        movingUp = false;
        movingDown = true;
        movingLeft = false;
        movingRight = false;
    }


    public void moveLeft(GameCanvas canvas) {
        Rectangle2D.Double nextPosition = new Rectangle2D.Double(getXPosition() - 5, getYPosition(), 50, 50);
        for (GameObject o: canvas.getGameObjectList()) {
            if (o instanceof Wall) {
                if (nextPosition.intersects(o.getHitbox())) return;
            }
        }
        setXPosition(getXPosition() - 5);

        movingUp = false;
        movingDown = false;
        movingLeft = true;
        movingRight = false;
    }

    public void moveRight(GameCanvas canvas) {
        Rectangle2D.Double nextPosition = new Rectangle2D.Double(getXPosition() + 5, getYPosition(), 50, 50);
        for (GameObject o: canvas.getGameObjectList()) {
            if (o instanceof Wall) {
                if (nextPosition.intersects(o.getHitbox())) return;
            }
        }
        setXPosition(getXPosition() + 5);

        movingUp = false;
        movingDown = false;
        movingLeft = false;
        movingRight = true;
    }

    public boolean placeBomb(GameCanvas canvas, GameFrame gf) {
        Bomb bomb;

        if (canPlaceBomb) {
            System.out.println("You have placed a bomb!");
            bomb = new Bomb(this.getXPosition(), this.getYPosition(), playerID);
            canvas.addGameObject(bomb);

            canPlaceBomb = false;

            javax.swing.Timer cooldown = new javax.swing.Timer(3000, ae -> {
                canPlaceBomb = true;
                ((javax.swing.Timer) ae.getSource()).stop();
            });
            cooldown.setRepeats(false);
            cooldown.start();
            return true;
            
        }
        else {
            System.out.println("Can't place bomb, cooldown.");
            return false;
        }
    }

    public int getScore() {
        return score;
    }

    public void setScore(int n) {
        score = n;
    }

    public void setLives(int n) {
        lives = n;
    }

    public int getNumLives() {
        return lives;
    }

    public int getPlayerID() {
        return playerID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    
}
