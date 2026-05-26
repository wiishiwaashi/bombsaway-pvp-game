public class GameStarter {
    public static void main(String[] args) {
        GameFrame gf = new GameFrame(1024, 768);
        gf.connectToServer();
        // gf.addKeyBindings();
        gf.setUpKeyListener();
        gf.setUpAnimationTimer();
        gf.setUpGUI();
    }
}