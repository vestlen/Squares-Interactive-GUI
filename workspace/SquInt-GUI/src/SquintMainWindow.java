import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

// Swing Program Template
@SuppressWarnings("serial")
public class SquintMainWindow extends JPanel implements KeyListener {

	public static final String TITLE = "...Squares Interactive GUI...";

	// Name-constants (DIM stands for Dimension)
	public static final int ROOM_DIM = 400;				// The pixel width and height of the room
	public static final int CANVAS_WIDTH = 480;	// the pixel width of the room
	public static final int CANVAS_HEIGHT = 560;		// the pixel height of the room
	public static final int LEFT_WALL_OFFSET = 40;
	public static final int TOP_WALL_OFFSET = 120;
	public static final int TILES_DIM = 10;						// the number of tiles (squares) in a row or column
	public static final int TILE_DIM = ROOM_DIM / TILES_DIM;	// the number of pixels per tile
	public static final int CANVAS_DIM = 40;					// the number of pixels per grid square
	public static final int NUM_SQUARES_ACROSS = CANVAS_WIDTH / CANVAS_DIM;
	public static final int NUM_SQUARES_DOWN = CANVAS_HEIGHT / CANVAS_DIM;	
	
	public Player player = null;

	// ......

	// private variables of GUI components
	// ......

	/** Constructor to setup the GUI components */
	public SquintMainWindow() {
		player = new Player(0,0,Player.DOWN, "vader", true);		
		
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

	/** Custom painting codes on this JPanel */
	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);  // paint background
		setBackground(Color.WHITE);
		Graphics2D g2d = (Graphics2D) g;
		
		drawGrid(g);	// Draw the room grid
		drawPlayer(player.x, player.y, g2d);	// Draw the player
		// Draw the player
		// Your custom painting codes
		// ......
	}

	/**
	 * Draws the gridlines to define tiles in the room
	 * 
	 * @param numTilesPerRowAndCol		the number of tiles per row and per column
	 * @param g							the graphics object
	 */
	private void drawGrid(Graphics g) {	     	
		
		Point[][] roomCoords = new Point[NUM_SQUARES_DOWN][NUM_SQUARES_ACROSS]; 	
		for (int row = 0; row < NUM_SQUARES_DOWN; row++) {
			for (int col = 0; col < NUM_SQUARES_ACROSS; col++) {	
				roomCoords[row][col] = new Point(col * CANVAS_DIM, row * CANVAS_DIM);				
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
		String[][][] room = new String[][][] {
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
		
		for (int layer = 0; layer < room.length; layer++) {
			for (int row = 0; row < NUM_SQUARES_DOWN; row++) {
				for (int col = 0; col < NUM_SQUARES_ACROSS; col++) {
					// Allow for empty grid spots in case of larger images
					if ( room[layer][row][col] != "" ) {
						// If the image to be drawn to the grid is for shading we want to handle it differently
						if (room[layer][row][col].contains("shad")) {
							drawImageToGrid(room[layer][row][col], roomCoords[row][col].x, roomCoords[row][col].y, g, true);							
						} else {
							drawImageToGrid(room[layer][row][col], roomCoords[row][col].x, roomCoords[row][col].y, g, false);
						}
					}
				}
			}	
		}
	}
	
	private void drawImageToGrid(String fileURL, int x_coord, int y_coord, Graphics g, boolean isShading) {
		File imageSrc = new File("res/images/" + fileURL);		
		Image img = null;
		try {
			img = ImageIO.read(imageSrc);
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
		Graphics2D g2d = (Graphics2D) g;
		g2d.drawImage(img, x_coord, y_coord, null);
	}
	
//	private int translateCoords(int x, int y) {
//		return (int)(y * TILES_DIM) + x;
//	}

	private void drawPlayer(int tile_x, int tile_y, Graphics g) {	

		int player_x = tile_x * TILE_DIM + LEFT_WALL_OFFSET;
		int player_y = tile_y * TILE_DIM + TOP_WALL_OFFSET;	

		drawImageToGrid("avatars/" + player.avatar + "/" + player.direction + ".png", player_x, player_y, g, false);
	}
	
	private void moveRight() {
		if ( player.direction != Player.RIGHT ) {
			player.direction = Player.RIGHT;
		} else if ( player.x < TILES_DIM - 1 ) {
			player.x++;		
	        player.startMoveTimer();	
		}
	}
	
	private void moveUp() {
		if ( player.direction != Player.UP ) {
			player.direction = Player.UP;
		} else if ( player.y > 0 ) {
			player.y--;
	        player.startMoveTimer();
		}		
	}
	
	private void moveLeft() {
		if ( player.direction != Player.LEFT ) {
			player.direction = Player.LEFT;
		} else if ( player.x > 0 ) {
			player.x--;
	        player.startMoveTimer();
		}		
	}
	
	private void moveDown() {
		if ( player.direction != Player.DOWN ) {
			player.direction = Player.DOWN;
		} else if ( player.y < TILES_DIM - 1 ) {
			player.y++;
	        player.startMoveTimer();
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
	        	moveRight();
	        } else if(e.getKeyCode()== KeyEvent.VK_W || e.getKeyCode()== KeyEvent.VK_UP) {
	        	moveUp();
	        } else if(e.getKeyCode()== KeyEvent.VK_A || e.getKeyCode()== KeyEvent.VK_LEFT) {
	        	moveLeft();
	        } else if(e.getKeyCode()== KeyEvent.VK_S || e.getKeyCode()== KeyEvent.VK_DOWN) {
	        	moveDown();
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
