package project2demo2;

/*Project2 Breakout Game
Author: Ahmed Yussuf
Due: 03/21/2021
 */
import acm.graphics.*;
import acm.program.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

public class Project2Demo2 extends GraphicsProgram {

    final int PLAYING_WIDTH = 300;
    final int PLAYING_HEIGHT = 400;
    final String TATILE = "Breakout Game";
    GRect paddle;
    final double PADDLE_Y = PLAYING_HEIGHT - 10;
    final int paddleHeight = 8;
    final int paddleWidth = 45;
    final int PADDLE_Y_OFF_SET = 15;
    GLine top, bottom, left, right;
    GOval ball;
    final int ballSize = 10;
    int ballRadius = 5;
    GRect brick;

    int ROWS = 10;
    int COLS = 10;

    int BRICKS_Y_OFF_SET = 30;

    int row;
    int col;
    Color myColor;
    Clip clip;

    double ballX = PLAYING_WIDTH / 2;
    double ballY = PLAYING_HEIGHT / 2;

    int INITIAL_X_VEL = 3;
    int INITIAL_Y_VEL = 3;

    int START_PAUSE = 18;
    int brickCounts = 100;
    final String LABEL = "";
    final String FONT = "Times-14";
    int numTurns = 3;
    private int ballXChange, ballYChange;
    int score = 0;
    int pauseDuration = 19;
    int turnRemaining = numTurns;
    int brickRemaining = brickCounts;
    double ball_X_Start = PLAYING_WIDTH / 3;
    double ball_Y_Start = PLAYING_HEIGHT / 3;
    static boolean gameOn = false;
    javax.swing.Timer timer;

    //private AudioClip bounceClip = MediaTools.loadAudioClip("bounce.au");
    public static void main(String[] args) {
        new Project2Demo2().start();
    }

    @Override
    public void run() {
        // prompt the player to start the game
        GLabel myLabel = new GLabel("START TO CLICK THE MOUSE.");
        double labelWidth = myLabel.getWidth();
        myLabel.setFont(FONT);
        myLabel.move((PLAYING_WIDTH - labelWidth) / 2, PADDLE_Y / 2);
        myLabel.setColor(Color.white);
        add(myLabel);
        waitForClick();
        remove(myLabel);
        setVelocity();
        pause(START_PAUSE);
        try {
            musicPlay();
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException ex) {
            Logger.getLogger(Project2Demo2.class.getName()).log(Level.SEVERE, null, ex);
        }
        while (turnRemaining > 0 && brickRemaining > 0) {
            ballMove();
            clip.start();
        }
        clip.stop();
        Display();

    }


    @Override
    public void init() {
        //increasing the window size to slightly larger than the playing area.
        // increase both x & y coordinates of the playing area
        setSize(PLAYING_WIDTH + 40, PLAYING_HEIGHT + 40);

        // set title
        setTitle(TATILE);
        setBackground(Color.BLACK);

        drawSides();
        drawBricks();

        createPaddle();
        createBall();
        addMouseListeners();
    }

    private void drawSides() {
        // Draw horizontal lines on the outside edges of the playing area. 
        // By creating 4 boundary lines using x & y coordiantes of the playing area
        top = new GLine(0, 0, PLAYING_WIDTH, 0);
        top.setColor(Color.MAGENTA);
        add(top);
        right = new GLine(PLAYING_WIDTH, 0, PLAYING_WIDTH, PADDLE_Y);
        right.setColor(Color.MAGENTA);
        add(right);
        bottom = new GLine(0, PADDLE_Y, PLAYING_WIDTH, PADDLE_Y);
        bottom.setColor(Color.MAGENTA);
        add(bottom);
        left = new GLine(0, 0, 0, PADDLE_Y);
        left.setColor(Color.MAGENTA);
        add(left);
    }

    private void drawBricks() {
        // draw all bricks required to play the game
        // GRect argument x,y, width, & height
        int brickGap = 2;
        int brickWidth = (PLAYING_WIDTH / COLS - (brickGap * 2));
        int brickHeight = PLAYING_HEIGHT / 6 / COLS;
        for (row = 0; row < ROWS; row++) {
            for (col = 0; col < COLS; col++) {
                int brick_Y = row * (brickHeight + brickGap) + brickGap;
                int brick_X = col * (brickWidth + brickGap) + 3 * brickGap +5;
                brick = new GRect(brick_X, brick_Y, brickWidth, brickHeight);
                brick.setFilled(true);
                brick.setColor(Color.GREEN);
                add(brick);
            }
        }
    }

    private void createPaddle() {
        //To create the paddle by using the playing area height constant to calculate the paddle location
        // Draw the paddle at the bottom of the playing area and set the color
        paddle = new GRect(PLAYING_WIDTH / 2 - paddleWidth / 2, PADDLE_Y - PADDLE_Y_OFF_SET - paddleHeight, paddleWidth, paddleHeight);
        paddle.setColor(Color.cyan);
        paddle.setFilled(true);
        add(paddle);
    }
 
    private void createBall() {
        //draw the ball and center it. 
        ball = new GOval(PLAYING_WIDTH / 2, PLAYING_HEIGHT / 3, ballSize, ballSize);
        ball.setColor(Color.BLUE);
        ball.setFilled(true);
        add(ball);
    }

    @Override
    public void mouseMoved(MouseEvent me) {
        int mouseX = me.getX();
        paddle.setLocation(mouseX, PADDLE_Y - PADDLE_Y_OFF_SET - paddleHeight);
        // To modify the mouseX value to keep it from moving off the playing area.
        if (mouseX <= 0) {
            paddle.setLocation(0, PADDLE_Y - PADDLE_Y_OFF_SET - paddleHeight);
        }
        if (mouseX > PADDLE_Y) {
            //paddle.setLocation(PLAYING_WIDTH - paddleWidth, PADDLE_Y - PADDLE_Y_OFF_SET - paddleHeight);
        }
    } 

    private void setVelocity() {

        ballYChange = 3;
        Random r = new Random();
        ballXChange = r.nextInt(3) + 1;
        if (r.nextInt(2) == 0) {
            ballXChange *= -1;
        }
        Display() ;
    }

    private void ballMove() {
        int pause = pauseDuration;

        GObject myObj;

        //get the ball's x & y location
        ballX = ball.getX();
        ballY = ball.getY();

        myObj = getElementAt(ballX, ballY);

        if (myObj == null) // check left upper corner of the ball 
        {
            myObj = getElementAt(ballX + ballSize, ballY);
        }

        if (myObj == null) // check bottom left corner of the ball 
        {
            myObj = getElementAt(ballX, ballY + ballSize);
        }        
        if (myObj == null) // check botton right corner of the ball 
        {
            myObj = getElementAt(ballX + ballSize, ballY + ballSize);
        }

        if (myObj == null) {
        } else if (myObj == paddle || myObj == top) {
            ballYChange = -ballYChange;

        } else if (myObj == left || myObj == right) {
            //System.out.println("hit left");
            ballXChange = -ballXChange;

        } // if the ball hit the bottom it means that the player lose
        // reduce the reamining turn and reset the ball location
        else if (myObj == bottom) {
            ballX = ball_X_Start + 40;
            ballY = ball_Y_Start;
            //ball.setLocation(ballX, ballY);
            pause += START_PAUSE;
            turnRemaining--;
            clip.stop();
            GLabel myLabel = new GLabel("YOUR REMAINING TURN IS: " + turnRemaining);
            double labelWidth = myLabel.getWidth();
            myLabel.setFont(FONT);
            myLabel.move((PLAYING_WIDTH - labelWidth) / 2, PADDLE_Y / 2);
            myLabel.setColor(Color.blue);
            add(myLabel);
            waitForClick();
            remove(myLabel);
        } // if the ball hit the brick change the velocity
        //remove the brick from the screen and reduce the brick remaining
        else {
            ballYChange = -ballYChange;
            remove(myObj);
            brickRemaining--;
            //bounceClip.play();
            score++;
        }
        pause(pause);
        ballX += ballXChange;
        ballY += ballYChange;
        ball.setLocation(ballX, ballY);
    }

    private void Display() {
        if (turnRemaining <= 0 && brickRemaining > 0) {
            GLabel myLabel = new GLabel("SORRY YOU LOST.");
            myLabel.setFont(FONT);
            double labelWidth = myLabel.getWidth();
            myLabel.move((PLAYING_WIDTH - labelWidth) / 2, PADDLE_Y / 2);
            myLabel.setColor(Color.blue);
            add(myLabel);
            waitForClick();
            remove(myLabel);
            myLabel = new GLabel("YOUR SCORE IS: " + score);
            myLabel.setFont(FONT);
            myLabel.move((PLAYING_WIDTH - labelWidth) / 2, PADDLE_Y / 2);
            myLabel.setColor(Color.RED);
            add(myLabel);
            remove(ball);
            waitForClick();
            remove(myLabel);
            waitForClick();
            removeAll();
            myLabel = new GLabel("TRY AGAIN");
            myLabel.setFont(FONT);
            myLabel.move((PLAYING_WIDTH - labelWidth) / 2 + 15, PADDLE_Y / 2);
            myLabel.setColor(Color.RED);
            add(myLabel);

        }
        if (brickRemaining == 0) {
            GLabel myLabel = new GLabel("CONGRATULATION YOU WON!!!!.");
            myLabel.setFont(FONT);
            double labelWidth = myLabel.getWidth();
            myLabel.move((PLAYING_WIDTH - labelWidth) / 2, PADDLE_Y / 2);
            myLabel.setColor(Color.red);
            add(myLabel);
            waitForClick();
            remove(myLabel);
            myLabel = new GLabel("YOUR SCORE IS: " + score);
            myLabel.setFont(FONT);
            myLabel.move((PLAYING_WIDTH - labelWidth) / 2, PADDLE_Y / 2);
            myLabel.setColor(Color.RED);
            add(myLabel);
            waitForClick();
            removeAll();
        }
    }

    public void musicPlay() throws UnsupportedAudioFileException, IOException, LineUnavailableException {
        File file = new File("AlanWa.wav");
        AudioInputStream audioStream = AudioSystem.getAudioInputStream(file);
        clip = AudioSystem.getClip();
        clip.open(audioStream);
        clip.start();
        clip.loop(Clip.LOOP_CONTINUOUSLY);
    }
}