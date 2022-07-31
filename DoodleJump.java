import javax.swing.JFrame;
import javax.swing.JPanel;
import java.awt.Dimension;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.awt.Font;
import java.awt.Color;
import javax.imageio.ImageIO;
import java.util.ArrayList;
import java.util.List;

public class DoodleJump extends JFrame {
	private GamePanel gamePanel;

	public DoodleJump() {
		this.gamePanel = new GamePanel();
		this.setDefaultCloseOperation(EXIT_ON_CLOSE);
		this.setResizable(false);
		this.setTitle("Doodle Jump << In Java >>");
		this.add(this.gamePanel);
		this.pack();
		this.setLocationRelativeTo(null);
		this.setVisible(true);
	}

	public void startGame() {
		this.gamePanel.startGameLoop();
	}

	public static void main(String[] args) {
		new DoodleJump().startGame();
	}
}

class GLOBAL_VARIABLES {
	public static int WINDOW_WIDTH = 500;
	public static int WINDOW_HEIGHT = 800;
	public static int PLAYER_WIDTH = 40;
	public static int PLAYER_HEIGHT = 40;
	public static int TILE_WIDTH = 40;
	public static int TILE_HEIGHT = 10;
}

class GamePanel extends JPanel implements Runnable {
	private Thread gameThread;
	private KeyController keyController;
	private Player player;
	private Background background;
	private List<Tile> tiles;

	private int score;

	GamePanel() {
		this.keyController = new KeyController();
		this.player = new Player(200, 200, 40, 40);
		this.background = new Background(0, 0, GLOBAL_VARIABLES.WINDOW_WIDTH, GLOBAL_VARIABLES.WINDOW_HEIGHT);
		this.tiles = new ArrayList<>();
		for (int i = 0; i < 25; i++) {
			this.tiles.add(new Tile(
					getRandomNumber(GLOBAL_VARIABLES.TILE_WIDTH,
							GLOBAL_VARIABLES.WINDOW_WIDTH - GLOBAL_VARIABLES.TILE_WIDTH),
					getRandomNumber(GLOBAL_VARIABLES.TILE_HEIGHT,
							GLOBAL_VARIABLES.WINDOW_HEIGHT - GLOBAL_VARIABLES.TILE_HEIGHT),
					GLOBAL_VARIABLES.TILE_WIDTH,
					GLOBAL_VARIABLES.TILE_HEIGHT));
		}
		this.addKeyListener(this.keyController);
		this.setPreferredSize(new Dimension(GLOBAL_VARIABLES.WINDOW_WIDTH, GLOBAL_VARIABLES.WINDOW_HEIGHT));
		this.setDoubleBuffered(true);
		this.setFocusable(true);
		this.score = 0;
	}

	public void startGameLoop() {
		this.gameThread = new Thread(this);
		this.gameThread.start();
	}

	@Override
	public void run() {
		while (this.gameThread.isAlive()) {
			for (Tile tile : this.tiles) {
				tile.update();
				if (CollisionDetector.detectTileCollision(this.player, tile)) {
					this.player.jump();
				}
				if (tile.getCoordinateY() > GLOBAL_VARIABLES.WINDOW_HEIGHT) {
					this.tiles.set(this.tiles.indexOf(tile), new Tile(
							getRandomNumber(GLOBAL_VARIABLES.TILE_WIDTH,
									GLOBAL_VARIABLES.WINDOW_HEIGHT - GLOBAL_VARIABLES.TILE_HEIGHT),
							0,
							GLOBAL_VARIABLES.TILE_WIDTH,
							GLOBAL_VARIABLES.TILE_HEIGHT));
					this.score += 1;
				}
			}
			this.player.update();
			if (this.player.getCoordinateY() > GLOBAL_VARIABLES.WINDOW_HEIGHT) {
				this.player.setCoordinateY(0);
				this.score = 0;
			}
			repaint();
			try {
				Thread.sleep(1000 / 60);
			} catch (InterruptedException ex) {
			}
		}
	}

	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D g2 = (Graphics2D) g;
		this.background.draw(g2);
		for (Tile tile : this.tiles) {
			tile.draw(g2);
		}
		this.player.draw(g2);
		g2.setColor(new Color(255, 255, 100));
		g2.setFont(new Font("Consolas", Font.BOLD, 15));
		g2.drawString(String.format("Score : %d", this.score), 10, 30);
		g2.dispose();
	}

	private int getRandomNumber(int min, int max) {
		return (int) ((Math.random() * (max - min)) + min);
	}

}

class KeyController implements KeyListener {
	private static boolean aPressed, dPressed;

	public static boolean isGoingLeft() {
		return aPressed;
	}

	public static boolean isGoingRight() {
		return dPressed;
	}

	@Override
	public void keyTyped(KeyEvent e) {
	}

	@Override
	public void keyPressed(KeyEvent e) {
		switch (e.getKeyCode()) {
			case java.awt.event.KeyEvent.VK_A:
				aPressed = true;
				break;
			case java.awt.event.KeyEvent.VK_D:
				dPressed = true;
				break;
		}
	}

	@Override
	public void keyReleased(KeyEvent e) {
		switch (e.getKeyCode()) {
			case java.awt.event.KeyEvent.VK_A:
				aPressed = false;
				break;
			case java.awt.event.KeyEvent.VK_D:
				dPressed = false;
				break;
		}
	}
}

interface GameObject {
	public void update();

	public void draw(Graphics2D g2);
}

class TwoDimensionalObject {
	private int coordinateX, coordinateY, width, height;

	TwoDimensionalObject(int coordinateX, int coordinateY, int width, int height) {
		this.coordinateX = coordinateX;
		this.coordinateY = coordinateY;
		this.width = width;
		this.height = height;
	}

	public int getCoordinateX() {
		return this.coordinateX;
	}

	public int getCoordinateY() {
		return this.coordinateY;
	}

	public int getWidth() {
		return this.width;
	}

	public int getHeight() {
		return this.height;
	}

	public void setCoordinateX(int coordinateX) {
		this.coordinateX = coordinateX;
	}

	public void setCoordinateY(int coordinateY) {
		this.coordinateY = coordinateY;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public void setHeight(int height) {
		this.height = height;
	}

}

class Player extends TwoDimensionalObject implements GameObject {

	private BufferedImage sprites[];
	private int currentSpriteIndex, speed, jumpForce;
	private double gravityStrength, fallSpeed;

	Player(int coordinateX, int coordinateY, int width, int height) {
		super(coordinateX, coordinateY, width, height);
		this.sprites = new BufferedImage[] {
				SpriteLoader.loadBufferedImage("./sprites/left.png"),
				SpriteLoader.loadBufferedImage("./sprites/right.png")
		};
		this.currentSpriteIndex = 0;
		this.speed = 7;
		this.jumpForce = 7;
		this.gravityStrength = 0.13d;
		this.fallSpeed = 0d;
	}

	@Override
	public void update() {
		this.keepWithinBoundaries();
		this.applyGravity();
		if (KeyController.isGoingLeft()) {
			this.currentSpriteIndex = 0;
			this.setCoordinateX(this.getCoordinateX() - this.speed);

		}
		if (KeyController.isGoingRight()) {
			this.currentSpriteIndex = 1;
			this.setCoordinateX(this.getCoordinateX() + this.speed);
		}
	}

	private void keepWithinBoundaries() {
		if (this.getCoordinateX() < 0) {
			this.setCoordinateX(500);
		}
		if (this.getCoordinateX() > 500) {
			this.setCoordinateX(0);
		}
	}

	private void applyGravity() {
		this.fallSpeed += this.gravityStrength;
		this.setCoordinateY((int) (this.getCoordinateY() + this.fallSpeed));
	}

	public void jump() {
		if (this.fallSpeed > 0) {
			this.fallSpeed = 0;
			this.fallSpeed -= this.jumpForce;
		}
	}

	@Override
	public void draw(Graphics2D g2) {
		g2.drawImage(this.sprites[this.currentSpriteIndex], this.getCoordinateX(), this.getCoordinateY(),
				this.getWidth(), this.getHeight(), null);
	}

}

class SpriteLoader {
	public static BufferedImage loadBufferedImage(String path) {
		java.awt.image.BufferedImage bufferedImage = null;
		try {
			bufferedImage = ImageIO.read(new java.io.FileInputStream(path));
		} catch (IOException ex) {
			System.err.println(String.format("Could not find %s", path));
		}
		return bufferedImage;
	}
}

class Background extends TwoDimensionalObject implements GameObject {

	private BufferedImage backgroundImage;

	Background(int coordinateX, int coordinateY, int width, int height) {
		super(coordinateX, coordinateY, width, height);
		this.backgroundImage = SpriteLoader.loadBufferedImage("./sprites/background.png");
	}

	@Override
	public void update() {
	}

	@Override
	public void draw(Graphics2D g2) {
		g2.drawImage(this.backgroundImage, this.getCoordinateX(), this.getCoordinateX(), this.getWidth(),
				this.getHeight(), null);
	}

}

class Tile extends TwoDimensionalObject implements GameObject {
	private BufferedImage sprite;
	private double fallSpeed;

	Tile(int coordinateX, int coordinateY, int width, int height) {
		super(coordinateX, coordinateY, width, height);
		this.sprite = SpriteLoader.loadBufferedImage("./sprites/tile.png");
		this.fallSpeed = 2;
	}

	@Override
	public void update() {
		this.applyGravity();
	}

	private void applyGravity() {
		this.setCoordinateY((int) (this.getCoordinateY() + this.fallSpeed));
	}

	@Override
	public void draw(Graphics2D g2) {
		g2.drawImage(this.sprite, this.getCoordinateX(), this.getCoordinateY(), this.getWidth(), this.getHeight(),
				null);
	}

}

class CollisionDetector {
	public static boolean detectTileCollision(TwoDimensionalObject player, TwoDimensionalObject tile) {
		return (player.getCoordinateX() < tile.getCoordinateX() + tile.getWidth() &&
				player.getCoordinateX() + player.getWidth() > tile.getCoordinateX() &&
				player.getCoordinateY() < tile.getCoordinateY() + tile.getHeight() &&
				player.getHeight() + player.getCoordinateY() > tile.getCoordinateY());
	}
}
