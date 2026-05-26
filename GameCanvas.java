import java.awt.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;
import javax.swing.*;

public class GameCanvas extends JComponent {
    private int width;
    private int height;
    private java.util.List<GameObject> gameObjectList;
    private Rectangle2D.Double sceneArea;
    private Rectangle2D.Double boardArea;
    private boolean gameOver;

    private Player player1;
    private Player player2;

    private String timeLeft;
    private BufferedImage bombsAwayLogo;
    private ImageIcon p1WinsScreen;
    private ImageIcon p2WinsScreen;
    private ImageIcon tieScreen;

    private int p1FinalScore;
    private int p2FinalScore;
    private int deadPlayerID;

    public GameCanvas(int w, int h) {
        gameObjectList = new java.util.concurrent.CopyOnWriteArrayList<>();
        width = w;
        height = h;
        sceneArea = new Rectangle2D.Double(0, 0, width, height);
        boardArea = new Rectangle2D.Double(15, 65, 970, 580);
        timeLeft = "";
        p1WinsScreen = new ImageIcon("P1WinsGIF.gif"); // https://docs.oracle.com/javase/8/docs/api/javax/swing/ImageIcon.html
        p2WinsScreen = new ImageIcon("P2WinsGIF.gif"); // https://docs.oracle.com/javase/8/docs/api/javax/swing/ImageIcon.html
        tieScreen = new ImageIcon("TieScreenGIF.gif"); // https://docs.oracle.com/javase/8/docs/api/javax/swing/ImageIcon.html
        setPreferredSize(new Dimension(width, height));
        
        for (int r = 0; r < 12; r++) {
            if (r == 0 || r == 11) {
                for (int c = 0; c < 20; c++) {
                    gameObjectList.add(new Wall((c*50) + 12, (r*50) + 62));
                }
            }
            else {
                gameObjectList.add(new Wall(12, (r*50) + 62));
                gameObjectList.add(new Wall(962, (r*50) + 62));
            }
        }

        for (int i = 157; i < 900; i=i+130) {
            for (int b = 202; b < 500; b=b+130) {
                gameObjectList.add(new Wall(i, b));
            }
        }

        try { //https://www.geeksforgeeks.org/java/image-processing-in-java-read-and-write/
            bombsAwayLogo = ImageIO.read(new File("LogoImg.png"));
        } catch (Exception e) {
            System.out.println("Could not load Game Logo image");
        }

        p1WinsScreen.setImageObserver(this);
        p2WinsScreen.setImageObserver(this);
        tieScreen.setImageObserver(this);
    }

    public java.util.List<GameObject> getGameObjectList() {
        return gameObjectList;
    }


    public void addGameObject(GameObject o) {
        gameObjectList.add(o);
    }

    public void removeGameObject(GameObject o) {
        gameObjectList.remove(o);
    }

    public void setGameOver(int p1score, int p2Score, int deadPID) {
        if (gameOver) return;
        gameOver = true;
        p1FinalScore = p1score;
        p2FinalScore = p2Score;
        deadPlayerID = deadPID;

        repaint();

        javax.swing.Timer closeGameTimer = new javax.swing.Timer(45000, ae -> {
            System.exit(0);
            ((javax.swing.Timer) ae.getSource()).stop();
        });
        closeGameTimer.setRepeats(false);
        closeGameTimer.start();
    }

    public void setPlayers(Player p1, Player p2) {
        player1=p1;
        player2=p2;
    }

    public void setTimeLeft(int min, int sec) {
        timeLeft = String.format("%d : %02d", min, sec);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;

        if (player1 == null || player2 == null) {
            return;
        }

        g2d.setColor(Color.decode("#0b2850"));
        g2d.fill(sceneArea);
        g2d.setColor(Color.decode("#57a14c"));
        g2d.fill(boardArea);

        String p1NameLabel = String.format("Player %s", player1.getName());
        String p2NameLabel = String.format("Player %s", player2.getName());
        String p1ScoreLabel = String.format("%-5d pts", player1.getScore());
        String p2ScoreLabel = String.format("%-5d pts", player2.getScore());

        g2d.setFont(new Font("Arial", Font.BOLD, 22)); 
        g2d.setColor(Color.WHITE);

        g2d.drawString(p1NameLabel, 12, 690);
        g2d.drawString(p2NameLabel, 738, 690);

        g2d.setFont(new Font("Futura", Font.BOLD, 30)); 
        g2d.drawString(timeLeft, 480, 40);

        g2d.setFont(new Font("Arial", Font.BOLD, 28)); 
        g2d.drawString(p1ScoreLabel, 310, 725);
        g2d.drawString(p2ScoreLabel, 610, 725);

        g2d.drawImage(bombsAwayLogo, 430, 660, 150, 100, null);

        for (GameObject i: gameObjectList) {
            i.draw(g2d);
        }

        if (gameOver) {
            g2d.setColor(Color.decode("#0b2850"));
            g2d.fillRect(0, 0, width, height);

            ImageIcon gif; 

            if (deadPlayerID==1) {
                gif = p2WinsScreen;
            } else if (deadPlayerID == 2) {
                gif = p1WinsScreen;
            } else if (p1FinalScore > p2FinalScore) { 
                gif = p1WinsScreen;
            } else if (p2FinalScore > p1FinalScore) {
                gif = p2WinsScreen;
            } else {
                gif = tieScreen;
            }

            int gifW = gif.getIconWidth();
            int gifH = gif.getIconHeight();
            int x = (width - gifW) / 2;
            int y = (height - gifH) / 2;
            gif.paintIcon(this, g2d, x, y);

        }

        RenderingHints rh = new RenderingHints(
            RenderingHints.KEY_ANTIALIASING,
            RenderingHints.VALUE_ANTIALIAS_ON
        );
        g2d.setRenderingHints(rh);
    }
}
