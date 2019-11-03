package gameCore;

import java.awt.Color;
import java.awt.Graphics;
import javax.swing.JApplet;


public class GameApplet extends JApplet {
	
	TankWorld game;
	Thread gameThread;
	
    public void init() {
    	//this.setFocusable(true);
        setBackground(Color.white);
        game = TankWorld.getInstance();
        game.init();
        
        this.getRootPane().add("Center", game);
        game.setFocusable(true);
        game.requestFocusInWindow();
    }
    
    public void start() {
        gameThread = new Thread(game);
        gameThread.setPriority(Thread.MIN_PRIORITY);
        game.start();
    }
    
    public void paint(Graphics g) {
    	game.paint(g);
    }
}
