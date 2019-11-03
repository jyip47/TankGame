package myGames;

import java.awt.Image;
import java.awt.Point;

import gameCore.TankWorld;

/* BigExplosion plays when player dies*/
public class BigExplosion extends BackgroundObject {
	int timer;
	int frame;
	static Image animation[] = new Image[] {TankWorld.sprites.get("explosion_small"),
			TankWorld.sprites.get("explosion_large"),
			};
        
	public BigExplosion(Point location) {
		super(location, animation[0]);
		timer = 0;
		frame=0;
		TankWorld.sound.play("ResourcesTanks/Chapter10/Explostion_large.wav");
	}
	
	public void update(int w, int h){
    	super.update(w, h);
    	timer++;
    	if(timer%5==0){
    		frame++;
    		if(frame<7)
    			this.img = animation[frame];
    		else
    			this.show = false;
    	}

	}
        
        public boolean collision (GameObject obj) {
            return false;
        }
}
