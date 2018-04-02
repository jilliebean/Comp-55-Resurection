import java.awt.event.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Random;

import javax.swing.Timer;
import acm.graphics.*;
import acm.program.*;

public class Level extends GraphicsProgram {
	// ***Instance variables***
	int score, health, counter1 = 0;
	Random rand;
	Song song;
	Circle circle;
	ArrayList<Circle> circles;
	ArrayList<Character> characters;
	AudioPlayer player;
	boolean isPaused;
	String folder = "sounds/";
	String filename = "hotelCali.mp3";
	Timer timer = new Timer(500, this);

	public void createCircle() {
		//Generate random coordinate to put the circle at, don't care where yet
		// TODO: be smarter about where it spawns
		double xloc = 500 * rand.nextDouble();
		double yloc = 500 * rand.nextDouble();
		
		Circle toAdd;
		if (characters.size() > 0)
			toAdd = new Circle(characters.remove(0), song.getCircleSize(), xloc, yloc, song.getShrinkSpeed(), true);
		else // Make dummy circle object for testing
			toAdd = new Circle('7', song.getCircleSize(), xloc, yloc, song.getShrinkSpeed(), false);

		add(toAdd.getInnerCircle());
		add(toAdd.getOuterCircle());
		add(toAdd.getLabel());
		circles.add(toAdd);
		
	}

	public void run() {
		rand = new Random();
		song = new Song(filename, 15.0, 1.0, 9, "abcdefghijklmnopqrstuvwxyz"); // using all characters in alphabetical order for easy testing
		circles = new ArrayList<Circle>(); // Initializes ArrayList of Circles
		characters = new ArrayList<Character>(); // Initializes ArrayList of characters

		// Adds characters to the characters ArrayList
		// TODO: generate characters ArrayList from incoming Song data
		characters.add('a');
		characters.add('b');
		characters.add('c');

		// start audio and timer
		player = AudioPlayer.getInstance();
		startAudioFile();
		timer.start();
	}

	// ***member methods***
	/**
	 * Timer function, executed every time the timer ticks
	 */
	public void actionPerformed(ActionEvent e) {
		counter1++;

		if (counter1 % song.getTempo() == 0) {
			createCircle(); // Make a new circle every few ticks to test
		}

		if (circles.size() >= 1) {
			int count = 0;
			for (Circle circle : circles) {
				circle.shrink();
				// TODO: make circles get smaller on display
				if (circle.getOutSize() < 0) {
					circles.remove(circle);
					// TODO: remove circles from display
				} else {
					System.out.println(count + ": " + circle);
					count++;
				}
			}
			System.out.println();
		}
	}// actionPerformed

	public void startAudioFile() {
		isPaused = false;
		player.playSound(folder, filename);
		System.out.println("SOUND PLAYED");
	}// startAudioFile

	public void pauseAudio() {
		player.pauseSound(folder, filename);
		isPaused = true;
	}// pause

	public void resumeAudio() {
		if (isPaused) {
			player.playSound(folder, filename);
			isPaused = false;
		}
	}// resume

	public void restartAudio() {
		player.stopSound(folder, filename);
		player.playSound(folder, filename);
	}// restart

	@Override
	public void keyPressed(KeyEvent e) {
		;
	}// keyPressed

	@Override
	public void mouseClicked(MouseEvent e) {
		;
	}// mouseClicked

	// ***getters***
	public int getScore() {
		return score;
	}// getScore

}// Level