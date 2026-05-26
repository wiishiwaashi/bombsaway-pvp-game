import java.awt.geom.*;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class GameServer {
    private int numPlayers;
    private int maxPlayers;
    private ServerSocket serverSocket;

    private Socket p1Socket;
    private Socket p2Socket;
    private ReadFromClient p1ReadRunnable;
    private ReadFromClient p2ReadRunnable;
    private WriteToClient p1WriteRunnable;
    private WriteToClient p2WriteRunnable;

    private double p1x, p1y, p2x, p2y;
    private int p1NumLives, p2NumLives;
    private int p1Score, p2Score;

    private boolean vertSlimeIsAlive[];
    private boolean vertSlimeIsRespawning[];
    private double vertSlimeXPosition[];
    private double vertSlimeYPosition[];
    private boolean vertSlimeMovingDown[];

    private boolean horSlimeIsAlive[];
    private boolean horSlimeIsRespawning[];
    private double horSlimeXPosition[];
    private double horSlimeYPosition[];
    private boolean horSlimeMovingRight[];

    private boolean gamePlaying;

    private Bomb serverBomb1;
    private Bomb serverBomb2;

    private long p1InviTime;
    private long p2InviTime;

    private String p1Name;
    private String p2Name;

    public GameServer() {
        System.out.println("==== GAME SERVER ====");
        numPlayers = 0;
        maxPlayers = 2;

        p1x = 120;
        p1y = 120;
        p2x = 880;
        p2y = 120;

        p1InviTime = 0;
        p2InviTime = 0;

        p1NumLives = 5;
        p2NumLives = 5;

        vertSlimeIsAlive = new boolean[3];
        vertSlimeIsRespawning = new boolean[3];
        vertSlimeXPosition = new double[3];
        vertSlimeYPosition = new double[3];
        vertSlimeMovingDown = new boolean[3];

        horSlimeIsAlive = new boolean[3];
        horSlimeIsRespawning = new boolean[3];
        horSlimeXPosition = new double[3];
        horSlimeYPosition = new double[3];
        horSlimeMovingRight = new boolean[3];

        serverBomb1 = null;
        serverBomb2 = null;

        for (int i = 0; i < 3; i++) {
            vertSlimeIsAlive[i] = true;
            vertSlimeXPosition[i] = 220+(i*260);
            vertSlimeYPosition[i] = 300;
            vertSlimeMovingDown[i] = true;
        }

        for (int i = 0; i < 3; i++) {
            horSlimeIsAlive[i] = true;
            horSlimeXPosition[i] = 200;
            horSlimeYPosition[i] = 265 + (i*130);
            horSlimeMovingRight[i] = true;
        }

        try {
            serverSocket = new ServerSocket(6090);
        } catch (IOException ex) {
            System.out.println("IOException from GameServer constructor");
        }
    }

    public void acceptConnections() {
        try {
            System.out.println("Waiting for connections...");
            while (numPlayers < maxPlayers) {
                Socket s = serverSocket.accept();
                DataInputStream in = new DataInputStream(s.getInputStream());
                DataOutputStream out = new DataOutputStream(s.getOutputStream());
                numPlayers++;
                out.writeInt(numPlayers);
                String tempName = in.readUTF();
                System.out.println("Player #" + numPlayers + " has connected.");

                ReadFromClient rfc = new ReadFromClient(numPlayers, in);
                WriteToClient wtc = new WriteToClient(numPlayers, out);

                if(numPlayers == 1) {
                    p1Socket = s;
                    p1ReadRunnable = rfc;
                    p1WriteRunnable = wtc;
                    p1Name = tempName;
                }
                else {
                    p2Socket = s;
                    p2ReadRunnable = rfc;
                    p2WriteRunnable = wtc;
                    p2Name = tempName;
                    p1WriteRunnable.sendStartMsg();
                    p2WriteRunnable.sendStartMsg();
                    startGameTimer();
                    Thread readThread1 = new Thread(p1ReadRunnable);
                    Thread readThread2 = new Thread(p2ReadRunnable);
                    readThread1.start();
                    readThread2.start();
                    Thread writeThread1 = new Thread(p1WriteRunnable);
                    Thread writeThread2 = new Thread(p2WriteRunnable);
                    writeThread1.start();
                    writeThread2.start();
                }
            }   
            System.out.println("No longer accepting connections");   
        } catch (IOException ex) {
            System.out.println("IOException from acceptConnections" + "The error is" + ex);
        }
    }

    private class ReadFromClient implements Runnable {
        private int playerId;
        private DataInputStream dataIn;

        public ReadFromClient (int pid, DataInputStream in) {
            playerId = pid;
            dataIn = in;
            System.out.println("RFC" + playerId + "Runnable created");
        }

        public void run() {
            try {
                while(true) {
                    int command = dataIn.readInt();
                    if (command == 1) {
                        if(playerId == 1) {
                            p1x = dataIn.readDouble();
                            p1y = dataIn.readDouble();
                        }
                        else {
                            p2x = dataIn.readDouble();
                            p2y = dataIn.readDouble();
                        }
                    }
                    else if (command == 2) {
                        double bombX = dataIn.readDouble();
                        double bombY = dataIn.readDouble();
                        int ownerId = dataIn.readInt();

                        if (ownerId == 1) {
                            serverBomb1 = new Bomb(bombX, bombY, ownerId);
                        }

                        else {
                            serverBomb2 = new Bomb(bombX, bombY, ownerId);
                        }

                        if (playerId == 1) {
                            p2WriteRunnable.sendBombPlaced(bombX, bombY, ownerId);
                        }
                        else {
                            p1WriteRunnable.sendBombPlaced(bombX, bombY, ownerId);
                        }
                    }
                    else if (command == 3) {
                        double bombX = dataIn.readDouble();
                        double bombY = dataIn.readDouble();

                        if (playerId == 1) {
                            p2WriteRunnable.sendExplosion(bombX, bombY);
                        }
                        else {
                            p1WriteRunnable.sendExplosion(bombX, bombY);
                        }
                    }
                    
                }
            } catch (IOException ex) {
                System.out.println("IOException at run method from ReadFromClient class");
            }
    }
}

    private class WriteToClient implements Runnable {
        private int playerId;
        private DataOutputStream dataOut;

        public WriteToClient(int pid, DataOutputStream out) {
            playerId = pid;
            dataOut = out;
            System.out.println("WTC" + playerId + "Runnable created");
        }

        public void run() {
            try {
                while(true){

                    if (playerId == 1){
                        updateSlimes(); // TODO
                        checkNeedToExplode();
                        
                        dataOut.writeInt(1);
                        dataOut.writeDouble(p2x);
                        dataOut.writeDouble(p2y);
                        dataOut.writeInt(p2NumLives);
                        dataOut.writeInt(p2Score);
                        dataOut.writeInt(p1Score);
                        dataOut.writeInt(p1NumLives);

                        for (int i = 0; i < 3; i++) {
                        dataOut.writeBoolean(vertSlimeIsAlive[i]);
                        dataOut.writeDouble(vertSlimeXPosition[i]);
                        dataOut.writeDouble(vertSlimeYPosition[i]);
                        }

                        for (int i = 0; i < 3; i++) {
                        dataOut.writeBoolean(horSlimeIsAlive[i]);
                        dataOut.writeDouble(horSlimeXPosition[i]);
                        dataOut.writeDouble(horSlimeYPosition[i]);
                        }
                        dataOut.flush();
                    }
                    else {
                        updateSlimes();
                        checkNeedToExplode();
                        
                        dataOut.writeInt(1);
                        dataOut.writeDouble(p1x);
                        dataOut.writeDouble(p1y);
                        dataOut.writeInt(p1NumLives);
                        dataOut.writeInt(p1Score);
                        dataOut.writeInt(p2Score);
                        dataOut.writeInt(p2NumLives);

                        for (int i = 0; i < 3; i++) {
                        dataOut.writeBoolean(vertSlimeIsAlive[i]);
                        dataOut.writeDouble(vertSlimeXPosition[i]);
                        dataOut.writeDouble(vertSlimeYPosition[i]);
                        }

                        for (int i = 0; i < 3; i++) {
                        dataOut.writeBoolean(horSlimeIsAlive[i]);
                        dataOut.writeDouble(horSlimeXPosition[i]);
                        dataOut.writeDouble(horSlimeYPosition[i]);
                        }
                        dataOut.flush();
                    }
                    try {
                        Thread.sleep(25);
                    } catch (InterruptedException ex) {
                        System.out.println("Interrupted Exception from WTC run()");
                    }
                }
            } catch (IOException ex) {
                System.out.println("IOEx in WTC runnable");
            }
        }

        public void sendGameOver(int p1s, int p2s, int deadPlayerID) {
            try {
                dataOut.writeInt(4);
                dataOut.writeInt(p1s);
                dataOut.writeInt(p2s);
                dataOut.writeInt(deadPlayerID);
                dataOut.flush();
            } catch (Exception e) {
                System.out.println("Exception at sendGameOver method");
            }
        }

        public void sendStartMsg() {
            try {
                dataOut.writeUTF(p1Name);
                dataOut.writeUTF(p2Name);
                
                dataOut.writeUTF("We now have 2 players in the server. GO !!!");
            } catch (IOException e) {
                System.out.println("IOException in sendStartMsg");
            }
        }

        public void sendBombPlaced(double x, double y, int ownerId) {
            try {
                dataOut.writeInt(2);
                dataOut.writeDouble(x);
                dataOut.writeDouble(y);
                dataOut.writeInt(ownerId);
                dataOut.flush();
                
            } catch (IOException e) {
                System.out.println("Error in forwarding bomb" + e);
            }
        }

        public void sendExplosion(double x, double y) {
            try {
                dataOut.writeInt(3);
                dataOut.writeDouble(x);
                dataOut.writeDouble(y);
                dataOut.flush();
                
            } catch (IOException e) {
                System.out.println("Error in forwarding bomb" + e);
            }
        }
    }


    public void updateSlimes() {
        checkSlimePlayerDamage();
        for (int i = 0; i < 3; i++) {
            if (!vertSlimeIsAlive[i]) continue;

            if (vertSlimeMovingDown[i]) {
                vertSlimeYPosition[i] +=2;
            }
            else {
                vertSlimeYPosition[i] -=2;
            }

            if (vertSlimeYPosition[i] >= 500) {
                vertSlimeYPosition[i] = 500;
                vertSlimeMovingDown[i] = false;
            }
            if (vertSlimeYPosition[i] <= 202) {
                vertSlimeYPosition[i] = 202;
                vertSlimeMovingDown[i] = true;
            }
        }

        for (int i = 0; i < 3; i++) {
            if (!horSlimeIsAlive[i]) continue;

            if (horSlimeMovingRight[i]) {
                horSlimeXPosition[i] +=2;
            }
            else {
                horSlimeXPosition[i] -=2;
            }

            if (horSlimeXPosition[i] >= 850) {
                horSlimeXPosition[i] = 850;
                horSlimeMovingRight[i] = false;
            }
            if (horSlimeXPosition[i] <= 120) {
                horSlimeXPosition[i] = 120;
                horSlimeMovingRight[i] = true;
    
            }
        }
    }

    public void checkNeedToExplode() {
        long now = System.currentTimeMillis();

        if (serverBomb1 != null && now - serverBomb1.getPlantedTime() >= 3000) {
            explodeBomb(serverBomb1);

            double bombX = serverBomb1.getXPosition();
            double bombY = serverBomb1.getYPosition();
            p1WriteRunnable.sendExplosion(bombX, bombY);
            p2WriteRunnable.sendExplosion(bombX, bombY);

            serverBomb1 = null;
            
        }

        if (serverBomb2 != null && now - serverBomb2.getPlantedTime() >= 3000) {
            explodeBomb(serverBomb2);

            double bombX = serverBomb2.getXPosition();
            double bombY = serverBomb2.getYPosition();
            p1WriteRunnable.sendExplosion(bombX, bombY);
            p2WriteRunnable.sendExplosion(bombX, bombY);

            serverBomb2 = null;
        }
    }

    public void explodeBomb (Bomb b) {
        double bombX = b.getXPosition();
        double bombY = b.getYPosition();
        int bombOwner = b.getOwnerId();

        Rectangle2D.Double explosionArea = new Rectangle2D.Double(bombX - 50, bombY - 50, 150, 150);

        for (int i = 0; i < 3; i++) {
            if (vertSlimeIsAlive[i]) {
                Rectangle2D.Double slimeHitBox = new Rectangle2D.Double(vertSlimeXPosition[i], vertSlimeYPosition[i], 50, 50);

                if (explosionArea.intersects(slimeHitBox)) {

                    if (vertSlimeIsAlive[i]) {
                        vertSlimeIsAlive[i] = false;
                    }

                    if (bombOwner == 1) {
                        p1Score += 50;
                    }
                    else if (bombOwner == 2) {
                        p2Score += 50;
                    }

                    if (!vertSlimeIsRespawning[i]) {
                        vertSlimeIsRespawning[i] = true;
                        final int toRespawn = i;
                        new Thread(() -> { // https://www.geeksforgeeks.org/java/how-to-create-thread-using-lambda-expressions-in-java/
                            try {
                                Thread.sleep(5000);
                                vertSlimeIsAlive[toRespawn] = true;
                                vertSlimeIsRespawning[toRespawn] = false;
                            } catch (Exception e) {
                                System.out.println("Exception at explodeBomb method, vertical slimes");
                            }

                    }).start();

                    }
                }
            }
        }

        for (int i = 0; i < 3; i++) {
            if (horSlimeIsAlive[i]) {
                Rectangle2D.Double slimeHitBox = new Rectangle2D.Double(horSlimeXPosition[i], horSlimeYPosition[i], 50, 50);

                if (explosionArea.intersects(slimeHitBox)) {
                    horSlimeIsAlive[i] = false;

                    if (bombOwner == 1) {
                        p1Score += 50;
                    }
                    else if (bombOwner == 2) {
                        p2Score += 50;
                    }

                    if (!horSlimeIsRespawning[i]) {
                        horSlimeIsRespawning[i] = true;
                        final int toRespawn = i;
                        new Thread(() -> { // https://www.geeksforgeeks.org/java/how-to-create-thread-using-lambda-expressions-in-java/
                            try {
                                Thread.sleep(5000);
                                horSlimeIsAlive[toRespawn] = true;
                                horSlimeIsRespawning[toRespawn] = false;
                            } catch (Exception e) {
                                System.out.println("Exception in explodeBomb method, horizontal slimes");
                            }

                    }).start();

                    }
                }
            }
        }

        Rectangle2D.Double p1Hitbox = new Rectangle2D.Double(p1x, p1y, 50, 50);
        Rectangle2D.Double p2Hitbox = new Rectangle2D.Double(p2x, p2y, 50, 50);
        
        long now = System.currentTimeMillis();

        // https://stackoverflow.com/questions/46579653/how-to-add-a-cooldown-in-java
        if (explosionArea.intersects(p1Hitbox) && now >= p1InviTime) {
            p1NumLives--;
            p1InviTime = now + 1000;
        }

        if (explosionArea.intersects(p2Hitbox) && now >= p2InviTime) {
            p2NumLives--;
            p2InviTime = now + 1000;
        }

        checkGameOver();
    }

    public void checkGameOver() {

        if (p1NumLives <= 0) {
            endGame(p1Score, p2Score, 1);
        } else if (p2NumLives <= 0) {
            endGame(p1Score, p2Score, 2);
        }
    }

    public void endGame(int p1s, int p2s, int deadPlayerID) {
        if (!gamePlaying) return;

        gamePlaying = false;

        if (deadPlayerID==1) {
            p1WriteRunnable.sendGameOver(p1s, p2s, 1);
            p2WriteRunnable.sendGameOver(p1s, p2s, 1);
        } else if (deadPlayerID == 2) {
            p1WriteRunnable.sendGameOver(p1s, p2s, 2);
            p2WriteRunnable.sendGameOver(p1s, p2s, 2);
        } else { 
            p1WriteRunnable.sendGameOver(p1s, p2s, 0);
            p2WriteRunnable.sendGameOver(p1s, p2s, 0);
        }
        
    }

    /**
     * This starts a new Thread for the game to go on for 2 minutes.
     */
    public void startGameTimer() {
        gamePlaying = true;
        Thread gameTimer = new Thread(() -> { // https://www.geeksforgeeks.org/java/how-to-create-thread-using-lambda-expressions-in-java/
            try {
                Thread.sleep(120000);
                gamePlaying = false;
                p1WriteRunnable.sendGameOver(p1Score, p2Score, 0);
                p2WriteRunnable.sendGameOver(p1Score, p2Score, 0);
                
            } catch (Exception e) {
                System.out.println("Exception at startGameTimer");
            }
        });
        gameTimer.start();
    }

    /**
     * This checks for collision between slime and player, damages player and reduces a life, and then makes them invincible for a second to prevent auto-death.
     */
    public void checkSlimePlayerDamage() {
        Rectangle2D.Double p1Hitbox = new Rectangle2D.Double(p1x, p1y, 50, 50);
        Rectangle2D.Double p2Hitbox = new Rectangle2D.Double(p2x, p2y, 50, 50);

        for (int i = 0; i < 3; i++) {
            if (vertSlimeIsAlive[i]) {
                Rectangle2D.Double slimeHitbox = new Rectangle2D.Double(vertSlimeXPosition[i] - 5, vertSlimeYPosition[i] - 5, 60, 60);

                long now = System.currentTimeMillis();
                if (p1Hitbox.intersects(slimeHitbox) && now >= p1InviTime) { // https://stackoverflow.com/questions/46579653/how-to-add-a-cooldown-in-java
                    p1NumLives--;
                    p1InviTime = now + 1000;
                    checkGameOver();
                }

                if (p2Hitbox.intersects(slimeHitbox) && now >= p2InviTime) { // https://stackoverflow.com/questions/46579653/how-to-add-a-cooldown-in-java
                    p2NumLives--;
                    p2InviTime = now + 1000;
                    checkGameOver();
                }
            }
        }

        for (int i = 0; i < 3; i++) {
            if (horSlimeIsAlive[i]) {
                Rectangle2D.Double slimeHitbox = new Rectangle2D.Double(horSlimeXPosition[i], horSlimeYPosition[i], 50, 50);

                long now = System.currentTimeMillis();
                if (p1Hitbox.intersects(slimeHitbox) && now >= p1InviTime) { // https://stackoverflow.com/questions/46579653/how-to-add-a-cooldown-in-java
                    p1NumLives--;
                    p1InviTime = now + 1000;
                    checkGameOver();
                }

                if (p2Hitbox.intersects(slimeHitbox) && now >= p2InviTime) { // https://stackoverflow.com/questions/46579653/how-to-add-a-cooldown-in-java
                    p2NumLives--;
                    p2InviTime = now + 1000;
                    checkGameOver();
                }
            }
        }
    }

    
    /**
     * This starts the GameServer. It is the main method.
     * @param args
     */
    public static void main(String[] args) {
        GameServer gs = new GameServer();
        gs.acceptConnections();
    }
}