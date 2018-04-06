import java.awt.event.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Random;

import javax.swing.Timer;
import acm.graphics.*;
import acm.program.*;

public class Level extends GraphicsProgram implements KeyListener {
	// ***Instance variables***
	public static final int WINDOW_WIDTH = 800;
	public static final int WINDOW_HEIGHT = 480;
	int score, health = 100, numTicks = 0;
	Random rand;
	Song song;
	Circle circle;
	ArrayList<Circle> circles; // Stores the circles being displayed on the screen
	ArrayList<Character> characters; // Stores the characters to feed into the circles
	AudioPlayer player;
	boolean isPaused;
	String folder = "sounds/";
	String filename = "RainsItPours.mp3";
	Timer timer = new Timer(10, this); // Timer ticks 20 times per second
	
	private GLabel scoreLabel; // holds the score for now
	private GRect healthBar; // displays HP percentage
	private GRect emptyHPBar; // Empty HP Bar to show full
	
	private GRect testScreenRect;
	private double lastXloc = 0;
	private double lastYloc = 0;
	private double lastXloc2 = 0;
	private double lastYloc2 = 0;
	private double lastXloc3 = 0;
	private double lastYloc3 = 0;
	
	private int temp = 1;
	

	public void createCircle() {
		// Generate random coordinate to put the circle at, don't care where yet
		// TODO: be smarter about where it spawns
		
		double xloc = WINDOW_WIDTH * rand.nextDouble();
		double yloc = WINDOW_HEIGHT * rand.nextDouble();
		
		// keep circle created in side the screen by 100*60 pixel
		// make sure circles are not created outside the screen
		// make sure circles are not overlapped
		
		/*
		 *  TODO: This function keeps giving infinite loops on execution,
		 *  I fixed it so that it just accepts whatever number is given to it after 10 failed attempts
		 *  There is probably a more elegant solution but this works for now
		 *  Look into more about how the rand.nextDouble() function works for a solution
		 *  -- Race
		 */
		int tries = 0;
		while(xloc <= 100 || xloc >= (WINDOW_WIDTH-100) || 
				Math.abs(xloc - lastXloc) < 100 || Math.abs(xloc - lastXloc2) < 100 || 
				Math.abs(xloc - lastXloc3) < 100) {
			xloc = WINDOW_WIDTH * rand.nextDouble();
			
			tries+=1;
			if(tries>10) break;
		}
		tries = 0;
		while(yloc <= 60 || yloc >= (WINDOW_HEIGHT-60) || 
				Math.abs(yloc - lastYloc) < 100 || Math.abs(yloc - lastYloc2) < 100 || 
				Math.abs(xloc - lastYloc3) < 100) {
			yloc = WINDOW_HEIGHT * rand.nextDouble();
			
			tries+=1;
			if(tries>10) break;
		}
		
		if(temp == 1) {
			lastXloc = xloc;
			lastYloc = yloc;
		}
		else if(temp == 2){
			lastXloc2 = xloc;
			lastYloc2 = yloc;
		}
		else {
			lastXloc3 = xloc;
			lastYloc3 = yloc;
		}
		
		temp++;
		if(temp == 4) { temp = 1;}
		
		
		Circle toAdd;
		if (characters.size() > 0)
			toAdd = new Circle(characters.remove(0), song.getCircleSize(), xloc, yloc, song.getShrinkSpeed(), true);
		else // Make dummy circle object for testing
			toAdd = new Circle('7', song.getCircleSize(), xloc, yloc, song.getShrinkSpeed(), false);

		// Add shapes to screen from the Circle, then add the Circle to the ArrayList of
		// Circles
		add(toAdd.getInnerCircle());
		add(toAdd.getOuterCircle());
		add(toAdd.getLabel());
		circles.add(toAdd);

	}

	/**
	 * Creates an ArrayListof characters from given string
	 * 
	 * @param str
	 *            string to turn into ArrayList
	 */
	private void createCharArrList(String str) {
		characters = new ArrayList<Character>(); // reset ArrayList so we start from scratch

		for (int i = 0; i < str.length(); i++) {
			// if the current character in the string is a letter or number (ignores weird
			// stuff)
			if (Character.isLetter(str.charAt(i)) || Character.isDigit(str.charAt(i))) {
				characters.add(str.charAt(i));
			}
		}
	}

	public void run() {
		setSize(WINDOW_WIDTH, WINDOW_HEIGHT); // Arbitrary numbers so far for screen size
		addKeyListeners();
		setFocusable(true);
		requestFocus();
		addMouseListeners(this);
		
		scoreLabel = new GLabel(Integer.toString(score),15,30);
		scoreLabel.setFont(new Font("Arial",0,20));
		healthBar = new GRect(WINDOW_WIDTH-(health)-10,10,(health),25);
		healthBar.setFilled(true);
		healthBar.setFillColor(Color.GREEN);
		emptyHPBar = new GRect(WINDOW_WIDTH-(health)-10,10,(health),25);
		
		
		testScreenRect = new GRect(10, 10, 800-20, 480-20);
		
		// Make the background grey to make colors stand out more
		testScreenRect.setFillColor(Color.GRAY);
		testScreenRect.setFilled(true);
		
		add(testScreenRect);
		add(scoreLabel);
		add(healthBar);
		add(emptyHPBar);
		
		rand = new Random();
		// I picked random numbers that look nice for the timer values, will have to test more
		song = new Song(filename, 15.0, 0.075, 100, "abcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyz"); // using all characters in alphabetical
																					// order for easy testing
		circles = new ArrayList<Circle>(); // Initializes ArrayList of Circles
		characters = new ArrayList<Character>(); // Initializes ArrayList of characters

		// Turns the string from Song into an ArrayList of characters to feed into the
		// circles
		createCharArrList(song.getCircleList());

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
		numTicks++;

		// Create a new circle every interval of time specified by the song's Tempo
		if (numTicks % song.getTempo() == 0) {
			createCircle(); // Make a new circle
		}

		// If there are circles on the screen
		if (circles.size() >= 1) {
			int count = 0;

			// Have to use manual definition of for loop to avoid
			// ConcurrentModificationException
			for (int i = 0; i < circles.size(); i++) {

				// Shrink circle
				Circle circle = circles.get(i);
				circle.shrink();

				// If circles have shrunk to be the same size in and out
				if (circle.getOutSize() < 0) {
					if(circle.getRemoveCounter() == 0) {
						// Add the text displaying that you missed
						circle.getLabel().setLabel("MISS");
						circle.getLabel().setColor(Color.BLACK);
						circle.removeCircles();
						health-=10;
					}
					else {
						circle.setRemoveCounter(circle.getRemoveCounter() + 1);
					}
				}

				if (circle.getRemoveCounter() >= 20) {
					circles.remove(circle);
					circle.removeLabel();
				}
				
//				else { // If circles are still bigger out than in
//
//					if (counter1 % 20 == 0) { // Only display circles once per second
//						System.out.println(count + ": " + circle);
//						count++;
//					}
//				}
			}
		}
		
		scoreLabel.setLabel(Integer.toString(score)); // updates score label every tick
		scoreLabel.sendToFront(); // makes sure this is always on top of circles
		
		// Updates health bar every tick
		if (health > 100) health = 100; // Stop HP from growing above 100
		healthBar.setSize((health),25);
		healthBar.setLocation(WINDOW_WIDTH-(health)-10,10);
		
		// Changes HP bar color based on health
		if (health > 75)
			healthBar.setFillColor(Color.GREEN);
		else if (health > 50)
			healthBar.setFillColor(Color.ORANGE);
		else if (health > 25)
			healthBar.setFillColor(Color.YELLOW);
		else
			healthBar.setFillColor(Color.RED);


//		if (counter1 % 20 == 0)
//			System.out.println(); // Print a blank line after displaying current status of circles ArrayList
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
	public void keyTyped(KeyEvent e) { // using keyTyped to help ensure valid input
		boolean found = false; // tracks if we have found a matching circle
		
		// Iterate through all circles on the screen
		for(Circle circle : circles) {
			// if you pressed a key matching a circle who is still shrinking
			if(e.getKeyChar() == circle.getLetter() && circle.getRemoveCounter() == 0) { 
				// Add the text displaying that you pressed it right
				// Pick which text based on how small the outer circle was
				// TODO: update score on press based on difference too
				double size = circle.getOutSize(); // store outer size to a variable for efficiency
				double init = song.getCircleSize(); // store initial size for math
				
				// Note: all numbers are subject to change
				if (size <= (init / 100)) { // If you press in the last hundredth of the timer
					circle.getLabel().setLabel("PERFECT!");
					circle.getLabel().setColor(Color.WHITE);
					score+=100;
					health+=20;
				}
				else if (size <= (init / 10)) { // If you press between 9/10 and 99/100
					circle.getLabel().setLabel("AMAZING!");
					circle.getLabel().setColor(Color.CYAN);
					score+=50;
					health+=10;
				}
				else if (size <= (init / 5)) {  // If you press between 4/5 and 9/10
					circle.getLabel().setLabel("GREAT!");
					circle.getLabel().setColor(Color.GREEN);
					score+=25;
					health+=5;
				}
				else if (size <= (init / 2)) { // If you press between 1/2 and 4/5
					circle.getLabel().setLabel("GOOD!");
					circle.getLabel().setColor(Color.YELLOW);
					score+=1;
					
				}
				else { // If you press in the first half of the timer
					circle.getLabel().setLabel("OK!");
					circle.getLabel().setColor(Color.ORANGE);
					score+=5;
				}
				
				// hide the GOvals after updating the label
				circle.removeCircles();
				
				found = true; // Remember that we found the circle
			}
		}
		
		if(!found) { // if no match was found
			System.out.println("No match for "+e.getKeyChar()+"!"); // Print to console no match was found
			health-=10;
			// TODO: remove lives once they are implemented
		}else // if found
			System.out.println("Circle "+e.getKeyChar()+" removed!"); // Print to console no match was found
	}// keyPressed

	@Override
	public void mousePressed(MouseEvent e) {
		System.out.println("Mouse clicked at ("+e.getX()+","+e.getY()+")!");
	}// mouseClicked

	// ***getters***
	public int getScore() {
		return score;
	}// getScore

}// Level