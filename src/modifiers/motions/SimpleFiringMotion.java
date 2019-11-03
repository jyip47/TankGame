/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package modifiers.motions;

import myGames.Ship;
import gameCore.TankWorld;

public class SimpleFiringMotion extends SimpleMotion {
	public SimpleFiringMotion(int interval){
		super();
		this.fireInterval = interval;
	}
	
	public void read(Object theObject){
		super.read(theObject);
		
		Ship ship = (Ship) theObject;
		
		if(TankWorld.getInstance().getFrameNumber()%fireInterval==0){
			ship.fire();
		}
	}

}
