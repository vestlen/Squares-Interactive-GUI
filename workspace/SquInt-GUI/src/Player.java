import java.util.Arrays;
import java.util.Collections;
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
	public int playerIdx;
	
	public static List<String> avatars = Arrays.asList(
			"blond", "blond_spiky", "blonde_blue", "blonde_hairband", "blue", 
			"brown", "brown_green", "brown_mustache", "brown_ponytail", "dark_knight", 
			"glasses", "graying", "green", "pirate", 
			"purple", "red_mustache", "strawberry");
	
	public Player(MapSquare[][] sq, int direction, String avatar, boolean canIMove, int idx) {
		// Pick a pseudorandom location to place the player based on the given map
		Integer[] numRows = new Integer[sq.length];
		for (int i = 0; i < numRows.length; i++) {
			numRows[i] = i;
		}
		Collections.shuffle(Arrays.asList(numRows));	// Get a random ordering of valid rows
		boolean foundSpot = false;
		// Go through each row until we find a row with an open spot for a player
		findSpotLoop:
		for (int row : numRows) {
			Integer[] numCols = new Integer[sq[row].length];
			for (int i = 0; i < numCols.length; i++) {
				numCols[i] = i;
			}
			Collections.shuffle(Arrays.asList(numCols));	// Get a random ordering of valid rows
			for (int col : numCols) {
				if (sq[row][col].playerIdx != -1 && sq[row][col].isOccupied == false) {
					foundSpot = true;
					x = col;
					y = row;
					break findSpotLoop;
				}
			}
		}
		if (!foundSpot) {
			System.out.println("No room for the player");
			return;
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
		playerIdx = idx;
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
