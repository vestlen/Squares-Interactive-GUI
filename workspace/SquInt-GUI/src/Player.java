import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class Player {

	public static final int DOWN = 0;
	public static final int LEFT = 1;
	public static final int UP = 2;
	public static final int RIGHT = 3;
	
	public static final int MOVE_DELAY = 250; // in milliseconds
	public static final int NUM_PHASES = 5;
	
	public static final String DEFAULT_AVATAR = "green";

	// Player attributes
	public int x; 						// the x location of the player using tile coordinates
	public int y;						// the y location of the player using tile coordinates
	public int direction;
	public String avatar = null;
	public boolean allowedToMove;
	public int animatePhase;
	private Timer moveTimer = null;
	
	private List<String> avatars = Arrays.asList(
			"blond", "blond_spiky", "blonde_blue", "blonde_hairband", "blue", 
			"brown", "brown_green", "brown_mustache", "ponytail", "dark_knight", 
			"glasses", "graying", "green", "pink", "pirate", 
			"purple", "red_mustache", "strawberry");
	
	public Player(int tile_x, int tile_y, int direction, String avatar, boolean canIMove) {
		if (tile_x < 0 || tile_x > SquintMainWindow.NUM_TILES_ACROSS) {
			x = 0;
		} else {
			x = tile_x;
		}
		if (tile_y < 0 || tile_y > SquintMainWindow.NUM_TILES_DOWN) {
			y = 0;
		} else {
			y = tile_y;
		}
		if (direction < RIGHT || direction > DOWN) {
			this.direction = DOWN;
		} else {
			this.direction = direction;			
		}
		if (avatars.contains(avatar)) {
			this.avatar = avatar;
		} else {
			this.avatar = DEFAULT_AVATAR;
		}
		animatePhase = 0;
		allowedToMove = canIMove;
	}
	
	public void startMoveTimer() {		
		allowedToMove = false;
		moveTimer = new Timer();
		moveTimer.schedule(new TimerTask() {
			@Override
			public void run() {
				allowedToMove = true;
				moveTimer.cancel();
			}		
		}, MOVE_DELAY);
	}
	
}
