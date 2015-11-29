/**
 * Apple.java
 */
package net.rio.superHedge;

import android.content.Context;

/**
 * @author rio
 *
 */
class Apple extends Entity {

	/**
	 * 
	 * @param con Context from MainActivity
	 * @param pos The start position of the Apple
	 * @param id The ID of Entity
	 * @param data Data from map file
	 * 
	 */
	public Apple(Context con, int[] pos, int id, int... data) {
		super(con, 2, pos, new int[]{30, 40}, id, data);
		// TODO Auto-generated constructor stub
	}
	
	@Override
	void tick() {
		
		move(3, GRAVITY / 2);
		
	}

	@Override
	void touched(Entity ent) {
		if(ent.type == 0) {
			rule.removeEnt(GameRule.ENTITY_EAT, id);
		}
	}

}
