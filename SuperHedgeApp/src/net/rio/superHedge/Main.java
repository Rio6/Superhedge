/**
 * Main.java
 */
package net.rio.superHedge;

import java.io.IOException;

import android.app.Activity;
import android.content.Context;
import android.graphics.Point;
import android.hardware.*;
import android.media.*;
import android.os.*;
import android.view.*;
import android.widget.*;

/**
 * Main class in SuperHedge
 * @author rio
 *
 */
public class Main extends Activity implements SensorEventListener, View.OnTouchListener {
	
	static final int STAT_MENU = 0;	
	static final int STAT_GAME = 1;
	
	static int scrW = 0, scrH = 0;
	
	private SensorManager mgr;
	private Sensor sr;
	private FrameLayout lay;
	private Game game;
	private Menu menu;
	private static SoundPool snds;
	private final Handler han =  new Handler();
	
	private Runnable run;
	
	private static int[] sndId;
	
	private int curLevel;
	private int gameStat;
	private int levelCnt;
	private boolean isRunning;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		
		/*getting the screen size*/
		Display display = ((WindowManager) getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
		Point size = new Point();
		display.getSize(size);
		scrW = size.x;
		scrH = size.y;
		
		/*setting variables*/
		
		snds = new SoundPool(4, AudioManager.STREAM_MUSIC, 0);
		sndId = new int[4];
		
		sndId[0] = snds.load(this, R.raw.start, 1);
		sndId[1] = snds.load(this, R.raw.apple, 1);
		sndId[2] = snds.load(this, R.raw.die, 1);
		sndId[3] = snds.load(this, R.raw.win, 1);
		
		lay = new FrameLayout(this);
		setContentView(lay);
		
		menu = new Menu(this);
		
		try {
			levelCnt = getAssets().list("levels").length;
		} catch (IOException e) {
			levelCnt = 0;
			e.printStackTrace();
		}
		
		/*setting seneor*/
		mgr = (SensorManager) getSystemService(SENSOR_SERVICE);
		sr = mgr.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		
		/*setting runnable*/
		run = new Runnable() {
			public void run() {
				game.tick();
				if(isRunning)
					han.postDelayed(this, 10);
			}
		};
		
		showMenu();
		
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		if(gameStat == Main.STAT_GAME) 
			game.phoneMoved(((int) event.values[1]) * 2);	
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {}
	
	@Override
	protected void onResume() {
		mgr.registerListener(this, sr, SensorManager.SENSOR_DELAY_FASTEST);

		strtTick();
		
		super.onResume();
	}

	@Override
	protected void onPause() {
		mgr.unregisterListener(this);
		
		isRunning = false;
		
		super.onPause();
	}
	
	/**
	 * start the ticking of the game
	 */
	private void strtTick() {
		
		if(gameStat == Main.STAT_GAME && !isRunning) {
			isRunning = true;
			han.postDelayed(run, 10);
		}		
		
	}

	@Override
	public boolean onTouch(View v, MotionEvent eve) {
		
		if(eve.getAction() == MotionEvent.ACTION_DOWN) {
			if(eve.getX() < 60 && eve.getY() < 60)	//touched at top left corner
				game.pause();
			else
				game.screenTouched(1);
		} else if(eve.getAction() == MotionEvent.ACTION_UP) {
			game.screenTouched(0);
		}
	    return true;
	    
	}
	
	@Override
	public void onBackPressed() {
		if(gameStat == STAT_GAME)
			game.pause();
		else if(gameStat == STAT_MENU)
			finish();
	}
	
	/**
	 * create a new level
	 * @param stat there are died, newgame, nextlevel
	 * @see Game#HEG_NEWGAME
	 * @see Game#HEG_NEXT_LEVEL
	 * @see Game#HEG_DIED
	 */
	void newGame(int stat) {
		
		switch(stat) {
		case Game.HEG_DIED:
			GameRule.loseApl();
			break;
		case Game.HEG_NEWGAME:
			curLevel = 0;
			GameRule.resetApl();
			break;
		case Game.HEG_NEXT_LEVEL:
			curLevel++;
			GameRule.saveApl();
			break;
		}
		
		if(curLevel == levelCnt) {
			game.win();
			return;
		}
		
		Runnable sGame = new Runnable() {
			public void run() {
				gameStat = Main.STAT_GAME;
				strtTick();
				lay.removeAllViews();
				lay.addView(game);
				game.start();
			}
		};
		
		game = new Game(this, curLevel);
		game.setOnTouchListener(this);

		if(gameStat == Main.STAT_MENU) {
			hideMenu(sGame);			
		} else {
			sGame.run();
		}
	}

	/**
	 * show the start menu of the game
	 */
	void showMenu() {
		gameStat = Main.STAT_MENU;
		isRunning = false;
		
		lay.addView(menu);

		menu.animate().y(0).withEndAction(new Runnable() {
			public void run() {
				if(lay.getChildCount() > 1)
					lay.removeView(game);				
			}
		});
	}
	 /**
	  * hide the start menu 
	  * @param run action after hiding
	  */
	private void hideMenu(Runnable run) {
		menu.animate().y(scrH).withEndAction(run);
	}
	
	/**
	 * play sound
	 * @param snd 0 = start, 1 = apple, 2 = die, 3 = win
	 */
	static void playSnd(int snd) {
		snds.play(sndId[snd], 1.0f, 1.0f, 0, 0, 1.0f);
	}

}
