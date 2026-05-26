import javax.sound.sampled.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Scanner;
import java.io.*;
import java.net.*;


public class GameFrame {
    private int width;
    private int height;
    private JFrame frame;
    private GameCanvas gameCanvas;
    private Player me;
    private Player enemy;
    private Slime[] arrayOfVertSlimes;
    private Slime[] arrayOfHorSlimes;
    private Container contentPane;
    private JPanel cpPanel;
    private String name;
    private String ipAddress;
    private int portNum;
    private Socket sock;
    private int playerId;
    private Timer animationTimer;
    private Timer gameTimer;
    private int totalSecondsLeft;
    private boolean up, down, left, right;
    private ReadFromServer rfsRunnable;
    private WriteToServer wtsRunnable;
    private boolean vertSlimeIsAlive[];
    private double vertSlimeXPosition[];
    private double vertSlimeYPosition[];

    private boolean horSlimeIsAlive[];
    private double horSlimeXPosition[];
    private double horSlimeYPosition[];

    private boolean gameOver;


    public GameFrame(int w, int h) {
        Scanner scanIn = new Scanner(System.in);
        System.out.print("Enter IP address: ");
        ipAddress = scanIn.nextLine();
        System.out.print("Enter Port Number: ");
        portNum = Integer.parseInt(scanIn.nextLine());
        System.out.print("Enter your name: ");
        name = scanIn.nextLine();
        scanIn.close();
        up = false;
        down = false;
        left = false;
        right = false;

        gameOver = false;

        vertSlimeIsAlive = new boolean[3];
        vertSlimeXPosition = new double[3];
        vertSlimeYPosition = new double [3];

        horSlimeIsAlive = new boolean[3];
        horSlimeXPosition = new double[3];
        horSlimeYPosition = new double[3];

        width = w;
        height = h;
        frame = new JFrame();
        gameCanvas = new GameCanvas(width, height);
        contentPane = frame.getContentPane();
        cpPanel = (JPanel) contentPane;
    }


    public void setUpGUI() {
        cpPanel.add(gameCanvas);
        frame.setTitle("Final Project - Doromal - Lee");
        frame.setSize(width, height);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
        cpPanel.requestFocusInWindow();

        try { 
            AudioInputStream audioIn = AudioSystem.getAudioInputStream(new File("BgMusic.wav"));
            Clip clip = AudioSystem.getClip();
            clip.open(audioIn);
            clip.loop(Clip.LOOP_CONTINUOUSLY);
            clip.start();
        } catch (Exception e) {
            System.out.println("Exception at playing background music");
        }
    }


    public void setUpAnimationTimer() {
        int interval = 10;
        ActionListener al = new ActionListener() {
            public void actionPerformed (ActionEvent ae) {
                if (up) {
                    me.moveUp(gameCanvas);
                }
                else if (down) {
                    me.moveDown(gameCanvas);
                }
                else if (left) {
                    me.moveLeft(gameCanvas);
                }
                else if (right) {
                    me.moveRight(gameCanvas);
                }
                gameCanvas.repaint();
            }
            
        };
        animationTimer = new Timer (interval, al);
        animationTimer.start();
    }

    public void setUpKeyListener() {
        KeyListener kl = new KeyListener() {
            public void keyTyped (KeyEvent ke) {
            }

            public void keyPressed (KeyEvent ke) {
                int keyCode = ke.getKeyCode();

                switch (keyCode) {
                    case KeyEvent.VK_UP :
                        up = true;
                        break;
                    case KeyEvent.VK_DOWN :
                        down = true;
                        break;
                    case KeyEvent.VK_LEFT :
                        left = true;
                        break;
                    case KeyEvent.VK_RIGHT :
                        right = true;
                        break;
                    case KeyEvent.VK_SPACE :
                        boolean sendBomb = me.placeBomb(gameCanvas, GameFrame.this);
                        if (sendBomb) {
                            onBombPlacedByMe(me.getXPosition(), me.getYPosition());
                        }
                        break;      
                }
            }

            public void keyReleased (KeyEvent ke) {
                int keyCode = ke.getKeyCode();

                switch (keyCode) {
                    case KeyEvent.VK_UP :
                        up = false;
                        break;
                    case KeyEvent.VK_DOWN :
                        down = false;
                        break;
                    case KeyEvent.VK_LEFT :
                        left = false;
                        break;
                    case KeyEvent.VK_RIGHT :
                        right = false;
                        break;
                }
            }

            
        };
        cpPanel.addKeyListener(kl);
        cpPanel.setFocusable(true);
    }


    public void createGameTimer() {
        totalSecondsLeft = 120;

        gameTimer = new Timer(1000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (totalSecondsLeft > 0) {
                    totalSecondsLeft --;

                    int min = totalSecondsLeft / 60;
                    int sec = totalSecondsLeft % 60;

                    gameCanvas.setTimeLeft(min, sec);
                    gameCanvas.repaint();

                } else {
                    gameTimer.stop();
                }

            }

        });
        gameTimer.start();
    }


    public void createPlayer() {
        if(playerId == 1) {
            me = new Player(100, 120, 1, this);
            enemy = new Player(880, 120, 2, null);
            gameCanvas.addGameObject(me);
            gameCanvas.addGameObject(enemy);
            gameCanvas.setPlayers(me, enemy);
        }
        else {
            enemy = new Player(100, 120, 1, null);
            me = new Player(880, 120, 2, this);
            gameCanvas.addGameObject(enemy);
            gameCanvas.addGameObject(me);
            gameCanvas.setPlayers(enemy, me);
        }
    }


    public void createSlime() {
        arrayOfVertSlimes = new Slime[3];
        for (int i = 0; i < 3; i++) {
            arrayOfVertSlimes[i] = new Slime(220+(i*260), 300, i+1);
            gameCanvas.addGameObject(arrayOfVertSlimes[i]);
        }

        arrayOfHorSlimes = new Slime[3];
        for (int i = 0; i < 3; i++) {
            arrayOfHorSlimes[i] = new Slime(200, 265 + (i*130), i+4);
            gameCanvas.addGameObject(arrayOfHorSlimes[i]);
        }
        
    }


    public void connectToServer() {
        try {
            sock = new Socket(ipAddress, portNum);
            DataInputStream in = new DataInputStream(sock.getInputStream());
            DataOutputStream out = new DataOutputStream(sock.getOutputStream());
            playerId = in.readInt();
            createPlayer();
            createSlime();
            createLives();
            out.writeUTF(name);
            System.out.println("You are player #" + playerId + ".");
            rfsRunnable = new ReadFromServer(in);
            wtsRunnable = new WriteToServer(out);
            rfsRunnable.waitForStartMsg();
        } catch (IOException ex) {
            System.out.println("IOException in connectToServer");
        }
    }


    private class ReadFromServer implements Runnable {
        private DataInputStream dataIn;

        public ReadFromServer(DataInputStream in) {
            dataIn = in;
            System.out.println("RFS Runnable created.");
        }

        public void run() {
            try {
                while(true) {
                    int command = dataIn.readInt();
                    
                    if (command == 1) {
                        double enemyX = dataIn.readDouble();
                        double enemyY = dataIn.readDouble();
                        int enemyLives = dataIn.readInt();
                        int enemyScore = dataIn.readInt();
                        int myScore = dataIn.readInt();
                        int myLives = dataIn.readInt();

                        for (int i = 0; i <3; i++) {
                            vertSlimeIsAlive[i] = dataIn.readBoolean();
                            vertSlimeXPosition[i] = dataIn.readDouble();
                            vertSlimeYPosition[i] = dataIn.readDouble();
                        }

                        for (int i = 0; i <3; i++) {
                            horSlimeIsAlive[i] = dataIn.readBoolean();
                            horSlimeXPosition[i] = dataIn.readDouble();
                            horSlimeYPosition[i] = dataIn.readDouble();
                        }

                        updateSlimes(vertSlimeIsAlive, vertSlimeXPosition, vertSlimeYPosition);
                        updateHorSlimes(horSlimeIsAlive, horSlimeXPosition, horSlimeYPosition);

                        int oldMeLives = me.getNumLives();
                        int oldEnemyLives = enemy.getNumLives();
                        if (enemy!=null) {
                            enemy.setXPosition(enemyX);
                            enemy.setYPosition(enemyY);
                            enemy.setLives(enemyLives);
                            enemy.setScore(enemyScore);
                        }

                        if(me != null) {
                            me.setScore(myScore);
                            me.setLives(myLives);
                        }

                        if(oldMeLives != myLives || oldEnemyLives != enemyLives){
                            refreshLives();
                        }
                        
                    }

                    else if (command == 2) {
                        double bombX = dataIn.readDouble();
                        double bombY = dataIn.readDouble();
                        int ownerId = dataIn.readInt();

                        Bomb enemyBomb = new Bomb(bombX, bombY, ownerId);
                        gameCanvas.addGameObject(enemyBomb);
                    }

                    else if (command == 3) {
                        double explosionX = dataIn.readDouble();
                        double explosionY = dataIn.readDouble();

                        for (GameObject o : gameCanvas.getGameObjectList()) {
                            if (o instanceof Bomb) { //https://www.geeksforgeeks.org/java/instanceof-keyword-in-java/
                                Bomb bomb = (Bomb) o;
                                if (Math.abs(bomb.getXPosition() - explosionX) < 10 && Math.abs(bomb.getYPosition() - explosionY) < 10) {
                                    bomb.explode(gameCanvas, null);
                                    new Thread(() -> {
                                        try {
                                            Thread.sleep(200);
                                            gameCanvas.removeGameObject(bomb);;
                                        }
                                        catch (Exception e) {
                                            System.out.println("Exceptio at run method in ReadFromServer class -- command 3");
                                        }
                                    }).start();
                                    break;
                                } 
                            }
                        }
                    }

                    else if (command == 4) {
                        if (gameOver) return;
                        gameOver = true;
                        
                        int p1FinalScore = dataIn.readInt();
                        int p2FinalScore = dataIn.readInt();
                        int deadPlayerID = dataIn.readInt();

                        if (playerId == 1) {
                            me.setScore(p1FinalScore);
                            enemy.setScore(p2FinalScore);
                        } else {
                            me.setScore(p2FinalScore);
                            enemy.setScore(p1FinalScore);
                        }

                        animationTimer.stop();
                        gameCanvas.setGameOver(p1FinalScore, p2FinalScore, deadPlayerID);
                    }
                }
            } catch (Exception e) {
                System.out.println("Error in RFS" + e.getMessage());
            }
        }

        public void waitForStartMsg() {
            try {

                String p1Name = dataIn.readUTF();
                String p2Name = dataIn.readUTF();

                if (playerId == 1) {
                    name = p1Name;
                    me.setName(p1Name);
                    enemy.setName(p2Name);

                } else {
                    name = p2Name;
                    enemy.setName(p1Name);
                    me.setName(p2Name);
                }
                
                String startMsg = dataIn.readUTF();
                System.out.println("Message from server: " + startMsg);
                Thread readThread = new Thread(rfsRunnable);
                Thread writeThread = new Thread(wtsRunnable);
                readThread.start();
                writeThread.start();
                createGameTimer();

            } catch(IOException ex) {
                System.out.println("IOException from waitForStartMsg()");
            }
        }
    }

    private class WriteToServer implements Runnable {
        
        private DataOutputStream dataOut;
        
        public WriteToServer(DataOutputStream out) {
            dataOut = out;
            System.out.println("WTS Runnable created");
        }

        public void run() {
            try {
                
                while(true) {
                    if(me!=null) {
                        dataOut.writeInt(1);
                        dataOut.writeDouble(me.getXPosition());
                        dataOut.writeDouble(me.getYPosition());
                        // dataOut.writeInt(me.getNumLives());
                        dataOut.flush();
                    }
                    try {
                        Thread.sleep(25);
                    } catch (InterruptedException ex) {
                        System.out.println("Interrupted Exception from WTS run()");
                    } 
                }
                
            } catch (IOException ex){
                System.out.println("IOException from WTS run()");
            }
        }

        public void sendBombPlaced(double x, double y) {
            try {
                dataOut.writeInt(2);
                dataOut.writeDouble(x);
                dataOut.writeDouble(y);
                dataOut.writeInt(playerId);
                dataOut.flush();
                
            } catch (Exception e) {
                System.out.println("Error in sending bomb" + e);
            }
        }

        public void sendExplosion(double x, double y) {
            try {
                dataOut.writeInt(3);
                dataOut.writeDouble(x);
                dataOut.writeDouble(y);
                dataOut.flush();
            } catch (Exception e) {
                System.out.println("Error in sending explosion" + e);
            }
        }
    }

    public void onBombPlacedByMe(double x, double y) {
        if (wtsRunnable != null) {
            wtsRunnable.sendBombPlaced(x, y);
        }
    }

    public void createLives() {
        // maybe make a thread to update the life
        if (playerId == 1) {
            for (int i = 0; i<me.getNumLives(); i++) {
                gameCanvas.addGameObject(new Lives(12 + (i*55),708));
            }

            for (int i = 0; i<enemy.getNumLives(); i++) {
                gameCanvas.addGameObject(new Lives(962 - (i*55),708));
            }
        } else {
            for (int i = 0; i<enemy.getNumLives(); i++) {
                gameCanvas.addGameObject(new Lives(12 + (i*55),708));
            }

            for (int i = 0; i<me.getNumLives(); i++) {
                gameCanvas.addGameObject(new Lives(962 - (i*55),708));
            }
        }
    }

    public void refreshLives() {
            for (GameObject o : gameCanvas.getGameObjectList()) {
                if (o instanceof Lives) {
                    gameCanvas.removeGameObject(o);;
                }
            }

            if (playerId == 1) {
                for (int i = 0; i < me.getNumLives(); i++) {
                    gameCanvas.addGameObject(new Lives(12 + (i*55), 708));
                }
                for (int i = 0; i < enemy.getNumLives(); i++) {
                    gameCanvas.addGameObject(new Lives(962 - (i*55), 708));
                }
            }
            else {
                for (int i = 0; i < enemy.getNumLives(); i++) {
                    gameCanvas.addGameObject(new Lives(12 + (i*55), 708));
                }
                for (int i = 0; i < me.getNumLives(); i++) {
                    gameCanvas.addGameObject(new Lives(962 - (i*55), 708));
                }
            }
        }

    public void updateSlimes (boolean[] alive, double[] x, double[] y) {
        for (int i = 0; i < arrayOfVertSlimes.length; i++) {
            Slime s = arrayOfVertSlimes[i];

            if (!alive[i] && s.checkIfAlive()) {
                s.die();
            }
            else if (alive[i] && !s.checkIfAlive()) {
                s.revive();
            }

            s.setXPosition(x[i]);
            s.setYPosition(y[i]);
        }
    }
    
    public void updateHorSlimes (boolean[] alive, double[] x, double[] y) {
        for (int i = 0; i < arrayOfHorSlimes.length; i++) {
            Slime s = arrayOfHorSlimes[i];
            
            if (!alive[i] && s.checkIfAlive()) {
                s.die();
            }
            else if (alive[i] && !s.checkIfAlive()) {
                s.revive();
            }

            s.setXPosition(x[i]);
            s.setYPosition(y[i]);
        }
    }

    
}
