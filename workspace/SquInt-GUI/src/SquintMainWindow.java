import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

// Swing Program Template
@SuppressWarnings("serial")
public class SquintMainWindow extends JPanel implements KeyListener {

	public static final String TITLE = "...Squares Interactive GUI...";

	public static final String AVATAR_SUBFOLDER = "re/";
	
	// Name-constants (DIM stands for Dimension)
	public static final int ROOM_WIDTH = 400;				// The pixel width of the roomSquaresImageURLs
	public static final int ROOM_HEIGHT = 440;
	public static final int CANVAS_WIDTH = 480;	// the pixel width of the roomSquaresImageURLs
	public static final int CANVAS_HEIGHT = 560;		// the pixel height of the roomSquaresImageURLs
	public static final int LEFT_WALL_OFFSET = 40;
	public static final int TOP_WALL_OFFSET = 120;
	public static final int NUM_TILES_ACROSS = 10;						// the number of tiles (squares) in a row or column
	public static final int NUM_TILES_DOWN = 10;
	public static final int CANVAS_DIM = 40;					// the number of pixels per grid square
	public static final int TILE_DIM = CANVAS_DIM;				// the number of pixels per tile
	public static final int NUM_SQUARES_ACROSS = CANVAS_WIDTH / CANVAS_DIM;
	public static final int NUM_SQUARES_DOWN = CANVAS_HEIGHT / CANVAS_DIM;	
	
	public Player player = null;
	
	public static final int ANIMATION_DELAY = 500; // in milliseconds
	public static final int ANIMATION_DELAY_STEP = ANIMATION_DELAY / Player.NUM_PHASES; // in milliseconds
//	private Timer animationTimer = null;
	private boolean phaseComplete = false;	
	ScheduledExecutorService executor = null;
	// ......

	// private variables of GUI components
	private static String[][][] roomSquaresImageURLs = null;
	private static Point[][] roomSquaresCoords = null;
	private BufferedImage roomBackgroundImage = null;

	/** Constructor to setup the GUI components */
	public SquintMainWindow() {
		// Initialize the player
		player = new Player(0,0,Player.DOWN, "graying", true);	
		initRoom();
		setPreferredSize(new Dimension(CANVAS_WIDTH, CANVAS_HEIGHT));
		
		addKeyListener(this);
        setFocusable(true);
        setFocusTraversalKeysEnabled(false);
		
		// "this" JPanel container sets layout
		// setLayout(new ....Layout());

		// Allocate the UI components
		// .....

		// "this" JPanel adds components
		// add(....)

		// Source object adds listener
		// .....
	}
	
	private void initRoom() {
		roomSquaresCoords = new Point[NUM_SQUARES_DOWN][NUM_SQUARES_ACROSS]; 	
		for (int row = 0; row < NUM_SQUARES_DOWN; row++) {
			for (int col = 0; col < NUM_SQUARES_ACROSS; col++) {	
				roomSquaresCoords[row][col] = new Point(col * CANVAS_DIM, row * CANVAS_DIM);				
			}
		}
		// Standard wall, floor, wall row
		final String[] wfw_row = new String[] {
			"in_walls/12.png", "in_floor/5.png", "in_floor/5.png", "in_floor/5.png", "in_floor/5.png", "in_floor/5.png", "in_floor/5.png", "in_floor/5.png", "in_floor/5.png", "in_floor/5.png", "in_floor/5.png", "in_walls/13.png"
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
				top_row,	// top row
				empty_row,	// top row
				empty_row,	// top row
				wfw_row,	// middle row
				wfw_row,
				wfw_row,
				wfw_row,
				wfw_row,
				wfw_row,
				wfw_row,
				wfw_row,
				wfw_row,
				wfw_row,
				{"in_walls/12.png", "in_floor/5.png", "in_floor/5.png", "in_floor/5.png", "in_floor/5.png", "in_floor/5.png", "in_floor/5.png", "in_floor/5.png", "in_floor/5.png", "in_floor/5.png", "in_floor/5.png", ""}, 	// bottom row
				{"in_walls/12.png", "in_floor/5.png", "in_floor/5.png", "in_floor/5.png", "in_floor/5.png", "in_floor/5.png", "in_floor/5.png", "in_floor/5.png", "in_floor/5.png", "in_floor/5.png", "", ""},	// bottom row
			},
			{
				empty_row,	// top row
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
				{"in_walls/14.png", "", "in_floor/5.png", "in_floor/5.png", "in_floor/5.png", "in_floor/5.png", "in_floor/5.png", "in_floor/5.png", "in_floor/5.png", "in_floor/5.png", "in_walls/15.png", ""},	// bottom row
				{"", "", "in_walls/17.png", "in_walls/17.png", "in_walls/17.png", "in_walls/17.png", "in_walls/17.png", "in_walls/17.png", "in_walls/17.png", "in_walls/17.png", "", ""},	// bottom row
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
		drawPlayer(player.x, player.y, g2d);	// Draw the player
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
	
	private void drawImageToGrid(String fileURL, int x_coord, int y_coord, Graphics g, boolean isShading, boolean scaleImage) {
		File imageSrc = new File("res/images/" + fileURL);		
		Image img = null;
		BufferedImage bimg = null;
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
			// replace the color with a transparent black
			Color shadeLevelOneColor = new Color(0x000000);
			Color shadeLevelTwoColor = new Color(0x7F007F);
			Color shadeLevelThreeColor = new Color(0xff00ff);
			img = Transparency.makeColorTransparent(img, shadeLevelOneColor, 80);
			img = Transparency.makeColorTransparent(img, shadeLevelTwoColor, 40);
			img = Transparency.makeColorTransparent(img, shadeLevelThreeColor, 20);
		}
		if (scaleImage) {
			Dimension scaledSize = getScaledDimension(new Dimension(bimg.getWidth(), bimg.getWidth()), new Dimension(TILE_DIM, TILE_DIM), true);
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

	private void drawPlayer(int tile_x, int tile_y, Graphics g) {	

		int player_x = tile_x * TILE_DIM + LEFT_WALL_OFFSET;
		int player_y = tile_y * TILE_DIM + TOP_WALL_OFFSET;	

		if (player.animatePhase > 0) {
			int animationStep = (TILE_DIM / Player.NUM_PHASES) * player.animatePhase;
			if (player.direction == Player.RIGHT) {
				player_x += animationStep;		
			} else if (player.direction == Player.UP) {
				player_y -= animationStep;								
			} else if (player.direction == Player.LEFT) {
				player_x -= animationStep;	
			} else if (player.direction == Player.DOWN) {	
				player_y += animationStep;	
			}				
			drawImageToGrid("avatars/" + AVATAR_SUBFOLDER + player.avatar + "/" + player.direction + "-" + ((player.animatePhase % 2) + 1) + ".png", player_x, player_y - TILE_DIM/2, g, false, true);
			phaseComplete = true;
		} else {
			drawImageToGrid("avatars/" + AVATAR_SUBFOLDER + player.avatar + "/" + player.direction + ".png", player_x, player_y - TILE_DIM/2, g, false, true);
		}
	}

		Runnable periodicTask = new Runnable() {
		    public void run() {
		        // Invoke method(s) to do the work
		       animatePLayer();
		    }
		};
	
	public void animatePLayer() {	
		boolean endPhases = false;
		if (player.animatePhase >= Player.NUM_PHASES) {
			endPhases = true;
		}		
		if (phaseComplete) {
			phaseComplete = false;
			player.animatePhase++;
			repaint();
		}	
		if (endPhases) {
			// Now that the animation has completed, we can consider the player to be at the new tile
			if (player.direction == Player.RIGHT) {
				player.x++;		
			} else if (player.direction == Player.UP) {
				player.y--;								
			} else if (player.direction == Player.LEFT) {
				player.x--;
			} else if (player.direction == Player.DOWN) {	
				player.y++;	
			}
			player.animatePhase = 0;	// Reset to default of no animation
			player.allowedToMove = true; // Let the player move again now that the animation is complete
			executor.shutdown();
			repaint();			
		}
	}

	private void movePlayer(int direction) {
		boolean animate = false;
		switch (direction) {
		case Player.RIGHT:
			if ( player.direction != Player.RIGHT ) {
				player.direction = Player.RIGHT;
			} else if ( player.x < NUM_TILES_ACROSS - 1 ) {
				animate = true;	
			}
			break;
		case Player.UP:
			if ( player.direction != Player.UP ) {
				player.direction = Player.UP;
			} else if ( player.y > 0 ) {
				animate = true;
			}	
			break;
		case Player.LEFT:
			if ( player.direction != Player.LEFT ) {
				player.direction = Player.LEFT;
			} else if ( player.x > 0 ) {
				animate = true;
			}		
			break;
		case Player.DOWN:
			if ( player.direction != Player.DOWN ) {
				player.direction = Player.DOWN;
			} else if ( player.y < NUM_TILES_DOWN - 1 ) {
				animate = true;
			}		
			break;
		}		
		if (animate) {
			player.allowedToMove = false;
			player.animatePhase = 1;	// Start the animation	
		    executor = Executors.newSingleThreadScheduledExecutor();
			executor.scheduleAtFixedRate(periodicTask, 0, ANIMATION_DELAY_STEP, TimeUnit.MILLISECONDS);		
		} else {
			player.allowedToMove = true;
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
		if (player.allowedToMove) {
	        if(e.getKeyCode()== KeyEvent.VK_D || e.getKeyCode()== KeyEvent.VK_RIGHT) {
	        	movePlayer(Player.RIGHT);
	        } else if(e.getKeyCode()== KeyEvent.VK_W || e.getKeyCode()== KeyEvent.VK_UP) {
	        	movePlayer(Player.UP);
	        } else if(e.getKeyCode()== KeyEvent.VK_A || e.getKeyCode()== KeyEvent.VK_LEFT) {
	        	movePlayer(Player.LEFT);
	        } else if(e.getKeyCode()== KeyEvent.VK_S || e.getKeyCode()== KeyEvent.VK_DOWN) {
	        	movePlayer(Player.DOWN);
	        }
	        repaint();
		}
        System.out.println("keyPressed");
	}

	@Override
	public void keyReleased(KeyEvent e) {
        System.out.println("keyReleased");
	}

	@Override
	public void keyTyped(KeyEvent e) {
        System.out.println("keyTyped");
	}
}
