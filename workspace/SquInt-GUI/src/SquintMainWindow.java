import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.awt.image.FilteredImageSource;
import java.awt.image.ImageFilter;
import java.awt.image.ImageProducer;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.imageio.ImageIO;
import javax.swing.GrayFilter;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

// Swing Program Template
@SuppressWarnings("serial")
public class SquintMainWindow extends JPanel implements KeyListener {

	public static final String TITLE = "...Squares Interactive GUI...";

	public static final String AVATAR_SUBDIR = "avatars/re/";
	public static final String IMAGES_DIR = "res/images/";
	public static final List<String> SOLIDS = Arrays.asList("wall");
	
	// Name-constants (DIM stands for Dimension)
//	public static final int ROOM_WIDTH = 400;			// The pixel width of the roomSquaresImageURLs
//	public static final int ROOM_HEIGHT = 440;
	public static final int CANVAS_WIDTH = 480;			// the pixel width of the application window
	public static final int CANVAS_HEIGHT = 560;		// the pixel height of the application window
//	public static final int LEFT_WALL_OFFSET = 40;
//	public static final int TOP_WALL_OFFSET = 120;
//	public static final int NUM_TILES_ACROSS = 10;		// the number of tiles (squares) in a row or column
//	public static final int NUM_TILES_DOWN = 10;
	public static final int MAP_DIM = 40;				// the number of pixels per grid square
//	public static final int TILE_DIM = MAP_DIM;			// the number of pixels per tile
	public static final int NUM_SQUARES_ACROSS = CANVAS_WIDTH / MAP_DIM;	// The logical width of the map
	public static final int NUM_SQUARES_DOWN = CANVAS_HEIGHT / MAP_DIM;		// The logical height of the map
	
	// AI - Disabled by default, set AI_MODE = true; if you want to run in AI mode (note that keyboard input is ignored during AI mode)
	private final boolean AI_MODE = false;			// Set this to make some dummies
	private Player[] ai_players = null;				// The list of dummies (it will be resized if there are too many requested players)
	private Timer autoMoveTimer = null;
	private static final int NUM_AI_PLAYERS = 100;	// The number of dummies you want
	public static final int AI_MOVE_DELAY = 1000; 	// in milliseconds (used to slow down or speed up the dummies' movements)
	
	// PLAYER
	public Player player = null;
	public int num_players = 0;
	public List<Integer> heldKeys = new ArrayList<Integer>();	// Used to make sure that other keypresses do not interrupt an action that should be repeated by holding a key down
	
	// ANIMATION 
	public static final int ANIMATION_DELAY = 250;	// in milliseconds
//	public static final int ANIMATION_DELAY_SPRINTING = 250;	// in milliseconds
	public static final int ANIMATION_DELAY_STEP = ANIMATION_DELAY / Player.NUM_PHASES; // in milliseconds
//	public static final int ANIMATION_DELAY_RUN = ANIMATION_DELAY_SPRINTING / Player.NUM_PHASES; // in milliseconds
	private boolean phaseComplete = false;	// whether the animation phase has completed	
	Hashtable<Integer, ScheduledExecutorService> executors = new Hashtable<Integer, ScheduledExecutorService>();	// Pair each animation executor with a playerId to make sure that each player gets their own executor	

	// MAP / LEVEL / ROOM
	private String[][][] roomSquaresImageURLs = null;
	private Point[][] roomSquaresCoords = null;
	private BufferedImage roomBackgroundImage = null;
	private MapSquare[][] mapSquares = null;

	/** Constructor to setup the GUI components */
	public SquintMainWindow() {
		initRoom();
		setPreferredSize(new Dimension(CANVAS_WIDTH, CANVAS_HEIGHT));
		
		addKeyListener(this);
        setFocusable(true);
        setFocusTraversalKeysEnabled(false);

		if (AI_MODE) {
			ai_players = new Player[NUM_AI_PLAYERS];
			List<String> avatars = Player.avatars;	// Gather the list of available avatars
			Collections.shuffle(avatars);			// Shuffle the list for randomization
			// Go through and create the specified number of players
			for (int ai = 0; ai < NUM_AI_PLAYERS; ai++) {
				// Each player is created in a random available location, 
				// has a unique avatar (unless more AI are requested that exist avatars), 
				// is facing a random direction, and is able to move
				ai_players[ai] = new Player(mapSquares, (int)(Math.random()*4), avatars.get(ai % avatars.size()), true, ++num_players);
				// in the case that there was no more room for players, the player would have a null avatar
				// if the player was not successfully created, then too many AI players were requested and
				// the array of ai_players should be resized, and the loop exited
				if (ai_players[ai].avatar == null) {
					Player[] newAI_players = new Player[ai];
					System.arraycopy(ai_players, 0, newAI_players, 0, ai);
					ai_players = newAI_players;
					break;
				}
				updateMap(mapSquares, ai_players[ai], true);					
			}
			// Configure a timer to automatically move the AI players
			autoMoveTimer = new Timer();
			autoMoveTimer.scheduleAtFixedRate(new TimerTask() {
				@Override
				public void run() {
					// Go through all ai players and make them move in a random direction
					for(Player p : ai_players) {
						if (p.allowedToMove) {
							movePlayer((int)(Math.random()*4), p);	
						}
						repaint();		
					}				
				}		
			}, 2000, AI_MOVE_DELAY);
		} else {
			// Create a new player and update the map to indicate their location
			player = new Player(mapSquares, Player.DOWN, "glasses", true, ++num_players);
			updateMap(mapSquares, player, true);			
		}		
	}
	
	private void initRoom() {
		roomSquaresCoords = new Point[NUM_SQUARES_DOWN][NUM_SQUARES_ACROSS]; 	
		for (int row = 0; row < NUM_SQUARES_DOWN; row++) {
			for (int col = 0; col < NUM_SQUARES_ACROSS; col++) {	
				roomSquaresCoords[row][col] = new Point(col * MAP_DIM, row * MAP_DIM);
			}
		}		
		
		// Standard wall, floor, wall row
		final String[] wfw_row = new String[] {
			"in_walls/12.png", "in_floor/5.png", "in_floor/5.png", "in_floor/5.png", "in_floor/5.png", "in_floor/5.png", "in_floor/5.png", "in_floor/5.png", "in_floor/5.png", "in_floor/5.png", "in_floor/5.png", "in_walls/13.png"
		};
		// Wall, special floor, wall row
		final String[] wsfw1_row = new String[] {
			"in_walls/12.png", "in_floor/5.png", "in_floor/1.png", "in_floor/5.png", "in_floor/0.png", "in_floor/4.png", "in_floor/5.png", "in_floor/2.png", "in_floor/5.png", "in_floor/3.png", "in_floor/5.png", "in_walls/13.png"
		};
		// Wall, special floor, wall row
		final String[] wsfw2_row = new String[] {
			"in_walls/12.png", "in_floor/1.png", "in_floor/4.png", "in_floor/1.png", "in_floor/3.png", "in_floor/2.png", "in_floor/4.png", "in_floor/2.png", "in_floor/3.png", "in_floor/5.png", "in_floor/2.png", "in_walls/13.png"
		};
		// Wall, special floor, wall row
		final String[] wsfw3_row = new String[] {
			"in_walls/12.png", "in_floor/1.png", "in_floor/4.png", "in_floor/4.png", "in_floor/3.png", "in_floor/2.png", "in_floor/4.png", "in_floor/5.png", "in_floor/3.png", "in_floor/5.png", "in_floor/2.png", "in_walls/13.png"
		};
		final String[] left_shade = new String[] {
			"", "in_shadows/6.png", "", "", "", "", "", "", "", "", "", "", ""
		};
		final String[] empty_row = new String[] {
			"", "", "", "", "", "", "", "", "", "", "", ""	
		};
		final String[] top_row = new String[] {
				"in_walls/0.png", "", "in_walls/3.png", "in_walls/1.png", "in_walls/1.png", "in_walls/1.png", "in_walls/1.png", "in_walls/1.png", "in_walls/1.png", "in_walls/1.png", "in_walls/4.png", ""	
		};
		roomSquaresImageURLs = new String[][][] {
			{
				empty_row,	// top row
				empty_row,	// top row
				empty_row,	// top row
				wsfw2_row,	// middle row
				wfw_row,
				wsfw1_row,
				wfw_row,
				wsfw3_row,
				wsfw1_row,
				wsfw2_row,
				wfw_row,
				wsfw1_row,
				{"", "in_floor/5.png", "in_floor/5.png", "in_floor/5.png", "in_floor/2.png", "in_floor/5.png", "in_floor/5.png", "in_floor/5.png", "in_floor/5.png", "in_floor/5.png", "in_floor/5.png", ""}, 	// bottom row
				{"", "", "", "", "in_floor/5.png", "in_floor/5.png", "in_floor/5.png", "in_floor/5.png", "", "", "", "", ""},	// bottom row (doorway flooring)
			},
			{
				top_row,	// top row
				empty_row,	// top row
				empty_row,	// top row
				{"", "in_shadows/0.png", "in_shadows/1.png", "in_shadows/1.png", "in_shadows/1.png", "in_shadows/1.png", "in_shadows/1.png", "in_shadows/1.png", "in_shadows/1.png", "in_shadows/1.png", "in_shadows/1.png", ""},
				left_shade,	// middle row
				left_shade,	// middle row
				left_shade,	// middle row
				left_shade,	// middle row
				left_shade,	// middle row
				left_shade,	// middle row
				left_shade,	// middle row
				left_shade,	// middle row
				{"in_walls/14.png", "", "", "", "", "", "", "", "", "", "in_walls/15.png", ""},	// bottom row
				{"", "", "in_walls/17.png", "in_walls/18.png", "in_walls/11.png", "in_shadows/3.png", "", "in_walls/10.png", "in_walls/18.png", "in_walls/17.png", "", ""},	// bottom row
			},
			{
				empty_row,	// top row
				empty_row,	// top row
				empty_row,	// top row
				empty_row,	// middle row
				empty_row,	// middle row
				empty_row,	// middle row
				empty_row,	// middle row
				empty_row,	// middle row
				empty_row,	// middle row
				empty_row,	// middle row
				empty_row,	// middle row
				empty_row,	// middle row
				{"", "in_shadows/8.png", "", "", "", "", "", "", "", "", "", "", ""},	// bottom row
				empty_row,	// bottom row
			}
		};
		mapSquares = new MapSquare[NUM_SQUARES_DOWN][NUM_SQUARES_ACROSS];
		for (int row = 0; row < NUM_SQUARES_DOWN; row++) {
			for (int col = 0; col < NUM_SQUARES_ACROSS; col++) {	
				// Check if any of the layers contains a wall, if so then create a MapSquare that cannot be occupied by a player
				// If any square in the first layer has an imageURL of "" then that means that it is a part of a larger object
				
				// If we run across a map square that has already been initialized (like in the case of a wall texture that takes up
				// more than one room square) then ignore this square and continue on to the next column
				if (mapSquares[row][col] != null) {
					continue;
				}
				findSolidsLoop:
				for (String[][] layer : roomSquaresImageURLs) {
					boolean isSolid = false;
					for(String solid : SOLIDS) {
						if (layer[row][col].contains(solid)) {	
							isSolid = true;
						}
					}
					if (isSolid) {		
						File imageSrc = new File(IMAGES_DIR + layer[row][col]);					
						BufferedImage bimg = null;
						try {
							bimg = ImageIO.read(imageSrc);
						} catch (IOException e) {
							e.printStackTrace();
						}
						if (bimg != null) {
							int numSquaresWide = bimg.getWidth() / MAP_DIM;
							int numSquaresTall = bimg.getHeight() / MAP_DIM;
							for (int c = 0; c < numSquaresWide; c++) {
								for (int r = 0; r < numSquaresTall; r++) {
									// Because of how the bottom left corner wall, wall image 14, is textured, the top right map square is not solid
									// the same goes for the bottom right corner wall, wall image 15
									if ((c == 1 && r == 0 && layer[row][col].contains("in_walls/14.png")) 
											|| (c == 0 && r == 0 && layer[row][col].contains("in_walls/15.png"))) 
									{
										continue;	// Skip this map square
									}
									mapSquares[row+r][col+c] = new MapSquare(true, MapSquare.SOLID, roomSquaresCoords[row][col], new Dimension(MAP_DIM, MAP_DIM));
								}	
							}
						}
						break findSolidsLoop;
					}
				}
				if (mapSquares[row][col] == null) {
					mapSquares[row][col] = new MapSquare(false, MapSquare.EMPTY, roomSquaresCoords[row][col], new Dimension(MAP_DIM, MAP_DIM));
				}
			}
		}	
		roomBackgroundImage = makeImage();
	}
	public BufferedImage makeImage() {
		BufferedImage bImg = new BufferedImage(CANVAS_WIDTH, CANVAS_HEIGHT, 
				BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2 = bImg.createGraphics();

		// background drawing here, the display that doesn't change
		drawGrid(g2);

		g2.dispose();
		return bImg;
	}

	/** Custom painting codes on this JPanel */
	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);  // paint background
		if (roomBackgroundImage != null) {
			g.drawImage(roomBackgroundImage, 0, 0, this);
		}
		Graphics2D g2d = (Graphics2D) g;		
		if (AI_MODE) {
			for(Player p : ai_players) {
				drawPlayer(p, g2d);
			}
		} else {
			drawPlayer(player, g2d);	// Draw the player
		}
		// Draw the player
		// Your custom painting codes
		// ......
	}

	/**
	 * Draws the gridlines to define tiles in the roomSquaresImageURLs
	 * 
	 * @param numTilesPerRowAndCol		the number of tiles per row and per column
	 * @param g							the graphics object
	 */
	private void drawGrid(Graphics g) {	    				
		for (int layer = 0; layer < roomSquaresImageURLs.length; layer++) {
			for (int row = 0; row < NUM_SQUARES_DOWN; row++) {
				for (int col = 0; col < NUM_SQUARES_ACROSS; col++) {
					// Allow for empty grid spots in case of larger images
					if ( roomSquaresImageURLs[layer][row][col] != "" ) {
						// If the image to be drawn to the grid is for shading we want to handle it differently
						if (roomSquaresImageURLs[layer][row][col].contains("shad")) {
							drawImageToGrid(roomSquaresImageURLs[layer][row][col], roomSquaresCoords[row][col].x, roomSquaresCoords[row][col].y, g, true, false);							
						} else {
							drawImageToGrid(roomSquaresImageURLs[layer][row][col], roomSquaresCoords[row][col].x, roomSquaresCoords[row][col].y, g, false, false);
						}
					}
				}
			}	
		}
	}
	
	/**
	 * Draw an image onto the room
	 * 
	 * @param fileURL		The path to the image file
	 * @param x_coord		the x coordinate of the top left corner where the image should be drawn (it is drawn from there to the bottom right)
	 * @param y_coord		the y coordinate of the top left corner where the image should be drawn
	 * @param g				the graphics object that we are drawing the image onto
	 * @param isShading		whether or not the image is being used to add shading
	 * @param scaleImage	whether or not to scale the image to fit inside a tile dimension (used to make scale player avatars)
	 */
	private void drawImageToGrid(String fileURL, int x_coord, int y_coord, Graphics g, boolean isShading, boolean scaleImage) {
		File imageSrc = new File(IMAGES_DIR + fileURL);		
		Image img = null;
		BufferedImage bimg = null;	// Used if the image needs to be scaled
		try {
			img = ImageIO.read(imageSrc);
			bimg = ImageIO.read(imageSrc);
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("Failed to read image file");
		}
		if (img == null) {
			return;
		}
		if (isShading) {			
			// Go through each shade level, represented by a unique color, and
			// replace the color with a specified opacity level
			Color shadeLevelOneColor = new Color(0x000000);
			Color shadeLevelTwoColor = new Color(0x7F007F);
			Color shadeLevelThreeColor = new Color(0xff00ff);
			img = Transparency.makeColorTransparent(img, shadeLevelOneColor, 0x65);
			img = Transparency.makeColorTransparent(img, shadeLevelTwoColor, 0x40);
			img = Transparency.makeColorTransparent(img, shadeLevelThreeColor, 0x20);
			// Once all colors have had their opacity modified, convert the image to a black grayscale
			ImageFilter filter = new GrayFilter(true, 0);  
			ImageProducer producer = new FilteredImageSource(img.getSource(), filter);  
			img = Toolkit.getDefaultToolkit().createImage(producer);  
		}
		if (scaleImage) {
			Dimension scaledSize = getScaledDimension(new Dimension(bimg.getWidth(), bimg.getWidth()), new Dimension(MAP_DIM, MAP_DIM), true);
			img = resizeToBig(img, scaledSize.width, scaledSize.height);
		}
		Graphics2D g2d = (Graphics2D) g;
		g2d.drawImage(img, x_coord, y_coord, null);
	}
	
//	private int translateCoords(int x, int y) {
//		return (int)(y * TILES_DIM) + x;
//	}
	public static Dimension getScaledDimension(Dimension imgSize, Dimension boundary, boolean scaleUp) {

	    int original_width = imgSize.width;
	    int original_height = imgSize.height;
	    int bound_width = boundary.width;
	    int bound_height = boundary.height;
	    int new_width = original_width;
	    int new_height = original_height;

	    if (scaleUp) {
		    // first check if we need to scale width
		    if (original_width < bound_width) {
		        //scale width to fit
		        new_width = bound_width;
		        //scale height to maintain aspect ratio
		        new_height = (new_width * original_height) / original_width;
		    }

		    // then check if we need to scale even with the new height
		    if (new_height < bound_height) {
		        //scale height to fit instead
		        new_height = bound_height;
		        //scale width to maintain aspect ratio
		        new_width = (new_height * original_width) / original_height;
		    }
	    } else {

		    // first check if we need to scale width
		    if (original_width > bound_width) {
		        //scale width to fit
		        new_width = bound_width;
		        //scale height to maintain aspect ratio
		        new_height = (new_width * original_height) / original_width;
		    }

		    // then check if we need to scale even with the new height
		    if (new_height > bound_height) {
		        //scale height to fit instead
		        new_height = bound_height;
		        //scale width to maintain aspect ratio
		        new_width = (new_height * original_width) / original_height;
		    }
	    }

	    return new Dimension(new_width, new_height);
	}
	
	private Image resizeToBig(Image originalImage, int biggerWidth, int biggerHeight) {
	    int type = BufferedImage.TYPE_INT_ARGB;


	    BufferedImage resizedImage = new BufferedImage(biggerWidth, biggerHeight, type);
	    Graphics2D g = resizedImage.createGraphics();

	    g.setComposite(AlphaComposite.Src);
	    g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
	    g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
	    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

	    g.drawImage(originalImage, 0, 0, biggerWidth, biggerHeight, this);
	    g.dispose();


	    return resizedImage;
	}

	private void drawPlayer(Player p, Graphics g) {	
		if (p == null) {
			return;
		}
		int player_x = p.x * MAP_DIM;// + LEFT_WALL_OFFSET;
		int player_y = p.y * MAP_DIM;// + TOP_WALL_OFFSET;	

		if (p.animatePhase > 0) {
			int animationStep = (MAP_DIM / Player.NUM_PHASES) * p.animatePhase;
			if (p.direction == Player.RIGHT) {
				player_x += animationStep;		
			} else if (p.direction == Player.UP) {
				player_y -= animationStep;								
			} else if (p.direction == Player.LEFT) {
				player_x -= animationStep;	
			} else if (p.direction == Player.DOWN) {	
				player_y += animationStep;	
			}				
			drawImageToGrid(AVATAR_SUBDIR + p.avatar + "/" + p.direction + "-" + ((p.animatePhase % 2) + 1) + ".png", player_x, player_y - MAP_DIM/2, g, false, true);
			phaseComplete = true;
		} else {
			drawImageToGrid(AVATAR_SUBDIR + p.avatar + "/" + p.direction + ".png", player_x, player_y - MAP_DIM/2, g, false, true);
		}
	}

	class Animation extends TimerTask{
		private Player p = null;
		private int speedToggle;
		
		public Animation(Player p) {
			this.p = p;
			speedToggle = p.speed;
		}
		public void run() {
			// If the player is walking, the animation will occur every other run()
			if (--speedToggle <= 0) { 
				speedToggle = p.speed;	// Update the speed of the player in case they started sprinting during the animation
				animatePlayer(p);
			}
		}
	}
	
	public void animatePlayer(Player p) {	
		if (p == null) {
			return;
		}
		boolean endPhases = false;
		if (p.animatePhase >= Player.NUM_PHASES) {
			endPhases = true;
		}		
		if (phaseComplete) {
			phaseComplete = false;
			p.animatePhase++;
			repaint();
		}	
		if (endPhases) {
			// Update the map to indicate the player has moved
			updateMap(mapSquares, p, false);
			// Now that the animation has completed, we can consider the player to be at the new tile
			if (p.direction == Player.RIGHT) {
				p.x++;		
			} else if (p.direction == Player.UP) {
				p.y--;								
			} else if (p.direction == Player.LEFT) {
				p.x--;
			} else if (p.direction == Player.DOWN) {	
				p.y++;	
			}
			// Update the map to indicate the player's new position
			updateMap(mapSquares, p, true);
			
			p.animatePhase = 0;	// Reset to default of no animation			
			p.allowedToMove = true; // Let the player move again now that the animation is complete
			executors.get(p.playerIdx).shutdown();
			repaint();			
		}
	}
	
	/**
	 * Updates the provided map to indicate whether a player has left or entered a map square
	 * 
	 * @param map		The map to update
	 * @param p			The player being moved
	 * @param occupied	Whether or not the player's position is being occupied or reset to not occupied
	 */
	private void updateMap(MapSquare[][] map, Player p, boolean occupied) {
		if (p == null) {
			return;
		}
		if (occupied) {
			map[p.y][p.x].isOccupied = true;
			map[p.y][p.x].playerIdx = p.playerIdx;
		} else {
			map[p.y][p.x].isOccupied = false;
			map[p.y][p.x].playerIdx = MapSquare.EMPTY;
		}
	}

	private void movePlayer(int direction, Player p) {
		if (p == null || !p.allowedToMove) {
			return;		// Make sure we have a player to move and that they are allowed to move
		}
		boolean animate = false;
		// If the player is not current facing the direction specified via
		// keyboard input, then turn them to face that direction
		if ( p.direction != direction ) {
			p.direction = direction;
		} else {
			if ( p.y < 0 || p.x < 0 || p.y >= mapSquares.length || p.x >= mapSquares[p.y].length ) {
				return;
			}
			// for each direction of movement, check to see that the destination square is not a wall and is not occupied by another player			
			switch (direction) {
			case Player.RIGHT:
				// Check array bounds
				if ( p.x + 1 >= mapSquares[p.y].length ) { 
					break;
				} else if ( mapSquares[p.y][p.x + 1].playerIdx != -1 && mapSquares[p.y][p.x + 1].isOccupied == false ) {
					animate = true;	
				}
				break;
			case Player.UP:
				// Check array bounds
				if ( p.y <= 0 || p.x < 0) { 
					break;
				} else if ( mapSquares[p.y - 1][p.x].playerIdx != -1 && mapSquares[p.y - 1][p.x].isOccupied == false ) {
					animate = true;	
				}
				break;
			case Player.LEFT:
				// Check array bounds
				if ( p.x <= 0  || p.y < 0) { 
					break;
				}else if ( mapSquares[p.y][p.x - 1].playerIdx != -1 && mapSquares[p.y][p.x - 1].isOccupied == false ) {
					animate = true;	
				}	
				break;
			case Player.DOWN:
				// Check array bounds
				if ( p.y + 1 >= mapSquares.length) { 
					break;
				}else if ( mapSquares[p.y + 1][p.x].playerIdx != -1 && mapSquares[p.y + 1][p.x].isOccupied == false ) {
					animate = true;	
				}
				break;
			}		
		}//if (mapSquares[p.x + 1][p.y] )
		if (animate) {
			p.allowedToMove = false;
			p.animatePhase = 1;	// Start the animation	
		    executors.put(p.playerIdx, Executors.newSingleThreadScheduledExecutor());
		    executors.get(p.playerIdx).scheduleAtFixedRate(new Animation(p), 0, ANIMATION_DELAY_STEP, TimeUnit.MILLISECONDS);		
		} else {
			p.allowedToMove = true;
		}
	}

	/** The entry main() method */
	public static void main(String[] args) {
		// Run GUI codes in the Event-Dispatching thread for thread safety
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				JFrame frame = new JFrame(TITLE);
				frame.setContentPane(new SquintMainWindow());
				frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				frame.setResizable(false);
				frame.pack();             // "this" JFrame packs its components
				frame.setLocationRelativeTo(null); // center the application window
				frame.setVisible(true);            // show it
			}
		});
	}

	@Override
	public void keyPressed(KeyEvent e) {
		if (AI_MODE) return;
		if (!heldKeys.contains(e.getKeyCode())) {
			heldKeys.add(new Integer(e.getKeyCode()));		
		}
		if (e.getKeyCode() == KeyEvent.VK_SHIFT) {
    		player.speed = Player.RUNNING;			
		}
    	if (player.allowedToMove) {
	        if(heldKeys.contains(KeyEvent.VK_D) || heldKeys.contains(KeyEvent.VK_RIGHT)) {
	        	movePlayer(Player.RIGHT, player);
	        } else if(heldKeys.contains(KeyEvent.VK_W) || heldKeys.contains(KeyEvent.VK_UP)) {
	        	movePlayer(Player.UP, player);
	        } else if(heldKeys.contains(KeyEvent.VK_A) || heldKeys.contains(KeyEvent.VK_LEFT)) {
	        	movePlayer(Player.LEFT, player);
	        } else if(heldKeys.contains(KeyEvent.VK_S) || heldKeys.contains(KeyEvent.VK_DOWN)) {
	        	movePlayer(Player.DOWN, player);
	        } else if(heldKeys.contains(KeyEvent.VK_Q)) {
	        	int modVal = Player.RIGHT + 1;
	        	player.direction = ((((player.direction-1) % modVal) + modVal) % modVal);
	        } else if(heldKeys.contains(KeyEvent.VK_E)) {
	        	player.direction = (player.direction + 1) % (Player.RIGHT + 1);
	        }
	        repaint();
		}
	}

	@Override
	public void keyReleased(KeyEvent e) {
		heldKeys.remove(new Integer(e.getKeyCode()));
    	if (e.getKeyCode() == KeyEvent.VK_SHIFT ) {
    		player.speed = Player.WALKING;
    	}
//        System.out.println("keyReleased");
	}

	@Override
	public void keyTyped(KeyEvent e) {
//        System.out.println("keyTyped");
	}
}
