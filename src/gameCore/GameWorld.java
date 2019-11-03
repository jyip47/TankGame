/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gameCore;

import java.awt.*;
import java.awt.image.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.ListIterator;
import java.util.Observable;
import java.util.Observer;
import java.util.Random;

import javax.swing.*;

import myGames.*;
import modifiers.*;
import modifiers.motions.MotionController;
import modifiers.weapons.AbstractWeapon;
import modifiers.weapons.PulseWeapon;
import ui.*;

// extending JPanel to hopefully integrate this into an applet
// but I want to separate out the Applet and Application implementations
public final class GameWorld extends JPanel implements Runnable, Observer {

    private Thread thread;
    
    // GameWorld is a singleton class!
    private static final GameWorld game = new GameWorld();
    public static final GameSounds sound = new GameSounds();
    public static final GameClock clock = new GameClock();
    //GameMenu menu;
    //public Level level;
   
    private BufferedImage bimg;
    int score = 0, life = 4;
    Point speed = new Point(0,1);
    Random generator = new Random();
    int sizeX, sizeY;
    
    /*Some ArrayLists to keep track of game things*/
    private ArrayList<BackgroundObject> background;
    private ArrayList<Ship> enemies;
    private ArrayList<SimpleBullet> friendlyBullets, enemyBullets;
    private ArrayList<PlayerShip> players, playersInPlay;
    private ArrayList<InterfaceObject> ui;
    private ArrayList<Ship> powerups;
    
    
    public static HashMap<String, Image> sprites;
    public static HashMap<String, MotionController> motions = new HashMap<String, MotionController>();

    // is player still playing, did they win, and should we exit
    boolean gameOver, gameWon, gameFinished;
    ImageObserver observer;
        
    // constructors makes sure the game is focusable, then
    // initializes a bunch of ArrayLists
    private GameWorld(){
        this.setFocusable(true);
        background = new ArrayList<BackgroundObject>();
        enemies = new ArrayList<Ship>();
        friendlyBullets = new ArrayList<SimpleBullet>();
        enemyBullets = new ArrayList<SimpleBullet>();
        players = new ArrayList<PlayerShip>();
        playersInPlay = new ArrayList<PlayerShip>();
        ui = new ArrayList<InterfaceObject>();
        powerups = new ArrayList<Ship>();
        
        sprites = new HashMap<String,Image>();
    }
    
    /* This returns a reference to the currently running game*/
    public static GameWorld getInstance(){
    	return game;
    }

    /*Game Initialization*/
    public void init() {
        setBackground(Color.white);
        loadSprites();
        
        //level = new Level(sizeX,sizeY);
        //clock.addObserver(level);
        //level.addObserver(this);
 
        gameOver = false;
        observer = this;

        addBackground(new Background(sizeX,sizeY,speed, sprites.get("water")));
        
        //menu = new GameMenu();
    }
    
    /*Functions for loading image resources*/
    private void loadSprites(){    	
	    sprites.put("island1", getSprite("Resources/island1.png"));
	    sprites.put("island2", getSprite("Resources/island2.png"));
	    sprites.put("island3", getSprite("Resources/island3.png"));
	    sprites.put("water", getSprite("Resources/water.png"));
	    
	    sprites.put("enemy1", getSprite("Resources/enemy1.png"));
	    sprites.put("enemy2", getSprite("Resources/enemy2.png"));
	    sprites.put("enemy3", getSprite("Resources/enemy3.png"));
	    sprites.put("enemy4", getSprite("Resources/enemy4.png"));
	    sprites.put("boss", getSprite("Resources/boss.png"));
	    
	    sprites.put("bullet", getSprite("Resources/bullet.png"));
	    sprites.put("enemybullet1", getSprite("Resources/enemybullet1.png"));
	    
	    sprites.put("player1", getSprite("Resources/myplane1.png"));
	    sprites.put("player2", getSprite("Resources/myplane2.png"));
	    
	    sprites.put("explosion1_1", getSprite("Resources/explosion1_1.png"));
		sprites.put("explosion1_2", getSprite("Resources/explosion1_2.png"));
		sprites.put("explosion1_3", getSprite("Resources/explosion1_3.png"));
		sprites.put("explosion1_4", getSprite("Resources/explosion1_4.png"));
		sprites.put("explosion1_5", getSprite("Resources/explosion1_5.png"));
		sprites.put("explosion1_6", getSprite("Resources/explosion1_6.png"));
	    sprites.put("explosion2_1", getSprite("Resources/explosion2_1.png"));
		sprites.put("explosion2_2", getSprite("Resources/explosion2_2.png"));
		sprites.put("explosion2_3", getSprite("Resources/explosion2_3.png"));
		sprites.put("explosion2_4", getSprite("Resources/explosion2_4.png"));
		sprites.put("explosion2_5", getSprite("Resources/explosion2_5.png"));
		sprites.put("explosion2_6", getSprite("Resources/explosion2_6.png"));
		sprites.put("explosion2_7", getSprite("Resources/explosion2_7.png"));
		
		sprites.put("life1", getSprite("Resources/life1.png"));
		sprites.put("life2", getSprite("Resources/life2.png"));
		
		sprites.put("gameover", getSprite("Resources/gameover.png"));
		sprites.put("powerup", getSprite("Resources/powerup.png"));
		sprites.put("youwon", getSprite("Resources/youWin.png"));
    }
    
    public Image getSprite(String name) {
        URL url = GameWorld.class.getResource(name);
        Image img = java.awt.Toolkit.getDefaultToolkit().getImage(url);
        try {
            MediaTracker tracker = new MediaTracker(this);
            tracker.addImage(img, 0);
            tracker.waitForID(0);
        } catch (Exception e) {
        }
        return img;
    }
    
    
    /********************************
     * 	These functions GET things	*
     * 		from the game world		*
     ********************************/
    
    public int getFrameNumber(){
    	return clock.getFrame();
    }
    
    public int getTime(){
    	return clock.getTime();
    }
    
    public void removeClockObserver(Observer theObject){
    	clock.deleteObserver(theObject);
    }
    
    public ListIterator<BackgroundObject> getBackgroundObjects(){
    	return background.listIterator();
    }
    
    public ListIterator<PlayerShip> getPlayers(){
    	return playersInPlay.listIterator();
    }
    
    public ListIterator<SimpleBullet> getFriendlyBullets(){
    	return friendlyBullets.listIterator();
    }
    
    public ListIterator<SimpleBullet> getEnemyBullets(){
    	return enemyBullets.listIterator();
    }
    
    public ListIterator<Ship> getEnemies(){
    	return enemies.listIterator();
    }
    
    public int countEnemies(){
    	return enemies.size();
    }
    
    public int countPlayers(){
    	return players.size();
    }
    
    public void setDimensions(int w, int h){
    	this.sizeX = w;
    	this.sizeY = h;
    }
    
    /********************************
     * 	These functions ADD things	*
     * 		to the game world		*
     ********************************/
    
    public void addBullet(SimpleBullet...newObjects){
    	for(SimpleBullet bullet : newObjects){
    		if(bullet.isFriendly())
    			friendlyBullets.add(bullet);
    		else
    			enemyBullets.add(bullet);
    	}
    }
    
    public void addPlayer(PlayerShip...newObjects){
    	for(PlayerShip player : newObjects){
    		players.add(player);
    		playersInPlay.add(player);
    		ui.add(new InfoBar(player,Integer.toString(players.size())));
    	}
    }
    
    // add background items (islands)
    public void addBackground(BackgroundObject...newObjects){
    	for(BackgroundObject object : newObjects){
    		background.add(object);
    	}
    }
    
    // add power ups to the game world
    public void addPowerUp(Ship powerup){
    	powerups.add(powerup);
    }
    /*
    public void addRandomPowerUp(){
    	// rapid fire weapon or pulse weapon
    	if(generator.nextInt(10)%2==0)
    		powerups.add(new PowerUp(generator.nextInt(sizeX), 1, new TankWeapon(5)));
    	else {
			powerups.add(new PowerUp(generator.nextInt(sizeX), 1, new PulseWeapon()));
		}
    }
    */
    // add enemies to the game world
    public void addEnemies(Ship...newObjects){
    	for(Ship enemy : newObjects){
    		enemies.add(enemy);
    		enemy.start();
    	}
    }
    
    public void addClockObserver(Observer theObject){
    	clock.addObserver(theObject);
    }
    
    // this is the main function where game stuff happens!
    // each frame is also drawn here
    public void drawFrame(int w, int h, Graphics2D g2) {
        ListIterator<?> iterator = getBackgroundObjects();
        while(iterator.hasNext()){
        	BackgroundObject obj = (BackgroundObject) iterator.next();
            obj.update(w, h);
            if(obj.getY()>h || !obj.show){
            	iterator.remove();
            }
            obj.draw(g2, this);
        }
        
        if (!gameFinished) {                        
            //update enemies
            iterator = getEnemies();
            while(iterator.hasNext()){
            	Ship enemy = (Ship) iterator.next();
            	// clear off enemies that move too far off screen
                if(enemy.getY()>h || enemy.getX()<-300 || enemy.getX()>w+300){
                	enemy.show=false;
                }
                // check enemy-friendly bullet collisions
            	ListIterator<SimpleBullet> bullets = getFriendlyBullets();
            	while(bullets.hasNext()){
            		SimpleBullet bullet = bullets.next();
            		if(enemy.collision(bullet)){
            			enemy.damage(bullet.getStrength());
            			if(!enemy.show){
            				bullet.getOwner().incrementScore(enemy.getStrength());
            			}
            			bullets.remove();
            		}
            	}
            	
            	// check enemy-player collisions
            	ListIterator<PlayerShip> players = getPlayers();
            	while(players.hasNext()){
            		PlayerShip player = players.next();
            		if(enemy.collision(player) && player.respawnCounter<=0){
            			player.incrementScore(enemy.getStrength());
            			player.damage(enemy.getStrength());
            			enemy.damage(player.getStrength());
            		}
            		if(player.isDead()){
            			players.remove();
	        			if(playersInPlay.size()==0){
	        				gameOver = true;
	        			}
            		}
            	}
            	if(enemy.show)
            		enemy.draw(g2, this);
            	else
            		iterator.remove();
            }
            
            // remove stray enemy bullets and draw
            iterator = getEnemyBullets();
            while(iterator.hasNext()){
            	SimpleBullet bullet = (SimpleBullet) iterator.next();
            	ListIterator<PlayerShip> players = getPlayers();
            	while(players.hasNext()){
            		PlayerShip player = players.next();
            		if(bullet.collision(player) && player.respawnCounter<=0){
            			player.damage(bullet.getStrength());
            			iterator.remove();
            		}
            		if(player.isDead()){
            			players.remove();
            			if(playersInPlay.size()==0){
            				gameOver = true;
            			}
            		}
            	}
                if(bullet.getY()>h+10 || bullet.getY()<-10){
                	iterator.remove();
                }
                bullet.draw(g2, this);
            }

            // remove stray friendly bullets and draw
            iterator = getFriendlyBullets();
            while(iterator.hasNext()){
            	SimpleBullet obj = (SimpleBullet) iterator.next();
                //obj.update(w, h);
                if(obj.getY()>h+10 || obj.getY()<-10){
                	iterator.remove();
                }
                obj.draw(g2, this);
            }
            
            // update players and draw
            iterator = getPlayers();
            while(iterator.hasNext()){
            	PlayerShip player = (PlayerShip) iterator.next();
                player.update(w, h);
                player.draw(g2, this);
            }
            
            // powerups
            iterator = powerups.listIterator();
            while(iterator.hasNext()){
            	Ship powerup = (Ship) iterator.next();
            	ListIterator<PlayerShip> players =  getPlayers();
            	while(players.hasNext()){
            		PlayerShip player = players.next();
            		if(powerup.collision(player)){
            			AbstractWeapon weapon = powerup.getWeapon();
            			player.setWeapon(weapon);
            			powerup.die();
            			iterator.remove();
            		}
            	}
            	powerup.draw(g2, this);
            }
            
            // interface stuff
            iterator = ui.listIterator();
            int offset = 0;
            while(iterator.hasNext()){
            	InterfaceObject object = (InterfaceObject) iterator.next();
            	object.draw(g2, offset, h);
            	offset += 300;
            }
        }
    	// end game stuff
        else{
    		g2.setColor(Color.WHITE);
    		g2.setFont(new Font("Calibri", Font.PLAIN, 24));
        	if(!gameWon){
        		g2.drawImage(sprites.get("gameover"), w/3-50, h/2, null);
        	}
        	else{
        		g2.drawImage(sprites.get("youwon"), sizeX/3, 100, null);
        	}
    		g2.drawString("Score", sizeX/3, 400);
    		int i = 1;
        	for(PlayerShip player : players){
        		g2.drawString(player.getName() + ": " + Integer.toString(player.getScore()), sizeX/3, 375+50*i);
        		i++;
        	}
        }
  
    }

    public Graphics2D createGraphics2D(int w, int h) {
        Graphics2D g2 = null;
        if (bimg == null || bimg.getWidth() != w || bimg.getHeight() != h) {
            bimg = (BufferedImage) createImage(w, h);
        }
        g2 = bimg.createGraphics();
        g2.setBackground(getBackground());
        g2.setRenderingHint(RenderingHints.KEY_RENDERING,
                RenderingHints.VALUE_RENDER_QUALITY);
        g2.clearRect(0, 0, w, h);
        return g2;
    }

    /* paint each frame */
    public void paint(Graphics g) {
        if(players.size()!=0)
        	clock.tick();
    	Dimension windowSize = getSize();
        Graphics2D g2 = createGraphics2D(windowSize.width, windowSize.height);
        drawFrame(windowSize.width, windowSize.height, g2);
        g2.dispose();
        g.drawImage(bimg, 0, 0, this);
    }

    /* start the game thread*/
    public void start() {
        thread = new Thread(this);
        thread.setPriority(Thread.MIN_PRIORITY);
        thread.start();
    }

    /* run the game */
    public void run() {
    	
        Thread me = Thread.currentThread();
        while (thread == me) {
        	this.requestFocusInWindow();
            repaint();
          
          try {
                thread.sleep(23); // pause a little to slow things down
            } catch (InterruptedException e) {
                break;
            }
            
        }
    }
    
    /* End the game, and signal either a win or loss */
    public void endGame(boolean win){
    	this.gameOver = true;
    	this.gameWon = win;
    }
    
    public boolean isGameOver(){
    	return gameOver;
    }
    
    // signal that we can stop entering the game loop
    public void finishGame(){
    	gameFinished = true;
    }
    

    /*I use the 'read' function to have observables act on their observers.
     */
	@Override
	public void update(Observable o, Object arg) {
		AbstractGameModifier modifier = (AbstractGameModifier) o;
		modifier.read(this);
	}
}
