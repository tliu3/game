import java.awt.*;        
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.*;

public class GPanel extends JPanel {
	
	private GamePlate game;
	private String nameStr = "";
	private JLabel level, weapon, kill, hp;
	private JTextField nameInput;
	private JButton pauseButton;
	private JComboBox gameMode;
	private JComboBox weaponType;
	
    private static int levelNum = 1;
    private final int levelMax = 5;
    
    private final int[] HP = {1,1,1,1,3};
    private final int[] KN = {1,1,1,2,2};
    private final int[][] WN = { {7,5,2,7,7},{0,0,2,3,3} };
    private final int[] WT = {1,1,2,2,2};
    private final String[] WS = {"N-Bomb","Beam"};
    private final double[] TM = {1,1,1,1.2,1.3};
    private final int[] TT = {1,1,1,1,2};   
	
	public GPanel(){
		setLayout(null);
		setBackground(new Color(247,226,117));
		setBorder(BorderFactory.createLineBorder(Color.GRAY, 2));
		setPreferredSize(new Dimension(1000, 620));
		
		level = new JLabel("LEVEL  " + levelNum);
		level.setFont(new Font("SansSerif", Font.BOLD, 30));
		add(level);
		level.setBounds(750,20,200,30);

		kill = new JLabel("Kill: ");
		add(kill);
		kill.setBounds(750,70,200,30);
		
		hp = new JLabel("HP: ");
		add(hp);
		hp.setBounds(750,90,200,30);
		
		weapon = new JLabel(WS[0] + ": ");
		add(weapon);
		weapon.setBounds(750,110,200,50);
		
		JLabel msg = new JLabel("Name of the one you hate:");
		add(msg);
		msg.setBounds(750,250,200,20);
		
		nameInput = new JTextField("");
		add(nameInput);
		nameInput.setBounds(750,270,200,30);
		
		gameMode = new JComboBox();
		gameMode.setActionCommand("Game Mode");
		gameMode.addItem("Challenge Mode");
		gameMode.addItem("Free Mode");
		gameMode.setSelectedIndex(0);
		add(gameMode);
		gameMode.setBounds(750,200,200,40);

		weaponType = new JComboBox();
		weaponType.setActionCommand("Weapon Type");
		add(weaponType);
		weaponType.setBounds(750,330,200,40);
		
		JButton newGameButton = new JButton("New Game");
		add(newGameButton);
		newGameButton.setBounds(750,470,200,50);
		
		pauseButton = new JButton("Pause");
		add(pauseButton);
		pauseButton.setBounds(750,540,200,50);

		game = new GamePlate();
		add(game);
		game.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
		game.setBounds(10,10,700,600);
		
		gameMode.addActionListener(game);
		weaponType.addActionListener(game);
		newGameButton.addActionListener(game);
		pauseButton.addActionListener(game);
		
	} // end of GPanel constructor
	
	// nested class of GamePlate
	private class GamePlate extends JPanel implements ActionListener{
		  
	    private Timer timer;        // Timer that drives the animation.
	    BufferedImage bg = null;    // Background Image
	
	    private int width, height;  // The size of the panel
	    private int[] numWeapon;    // Number of each type of weapon left
	    private int totWeapon;      // Total Number of all types of weapons
	    private int numKill;        // Number killing performed
	    private int wt;             // weapon type indicator
	    private boolean isStart;
	    private boolean isFreeMode = false;
	    private boolean goToNext = false;
	    private boolean isWin = false;
	
	    private Me me;          	// The me, bomb, and target objects are defined by
	    private Bomb bomb;          // nested classes Me, Bomb, Bomb2, Target, and TBomb.
	    private Bomb2 bomb2;
	    private Target target; 
	    private TBomb tbomb;

	    // GamePlate constructor
	    public GamePlate() {
	    	
	    	try {	// load backgroundImage
	        	bg = ImageIO.read(new File("bg.png"));
	        } catch (IOException e) {}
	    	
	    	// initiate numWeapon and numKill
	    	numWeapon = new int[2];
	    	reset();
	    	
	    	// setting up weapon(s)
	    	weaponType.removeAllItems();
	    	for(int i=0; i<WT[levelNum-1]; i++){
	    		weaponType.addItem(WS[i]);
	    	}
	    	weaponType.setSelectedIndex(0);

	        ActionListener action = new ActionListener() {
	            // Defines the action taken each time the timer fires.
	            public void actionPerformed(ActionEvent evt) {
	            	if (me != null) {
	            		me.updateForNewFrame();
	            		switch(wt){
	        	    	case 0:
	        	    		bomb.updateForNewFrame();
	        	    		break;
	        	    	case 1:
	        	    		bomb2.updateForNewFrame();
	        	    		break;
	        	    	}
	            		target.updateForNewFrame();
	            		if(tbomb != null){
	            			tbomb.updateForNewFrame();
	            		}
	            	}
	            	repaint();
	            }
	        };
	      
	        timer = new Timer(30, action);  // Fires every 30 milliseconds.
	
	        addMouseListener( new MouseAdapter() {
	            // Use mouse listener to request focus.
	        	public void mousePressed(MouseEvent evt) {
	        		if(isWin){
	        			return;
	        		}
	        		if(goToNext){
	        			reset();
	        		}
        			nameStr = nameInput.getText();
        			isStart = true;
        			timer.start();
        			repaint();
        			requestFocus();
	        	}
	        } );
	
	        addKeyListener( new KeyAdapter() {
	            // Use key listener to respond to keyPressed events on the panel.
	    	  	// The left- and right-arrow keys move me, while down-arrow releases the bomb.
	        	public void keyPressed(KeyEvent evt) {
	        		int code = evt.getKeyCode();
	        		if(!me.isHit){
	        			if (code == KeyEvent.VK_LEFT) {
		        			me.centerX -= 15;
		        		} else if (code == KeyEvent.VK_RIGHT) {  
		        			me.centerX += 15;
		        		} else if (code == KeyEvent.VK_DOWN) {
		        			switch(wt){
		        	    	case 0:
		        	    		if (numWeapon[0] > 0 && bomb.isFalling == false)
			        				bomb.isFalling = true;
		        	    		break;
		        	    	case 1:
		        	    		if (numWeapon[1] > 0 && bomb2.isFalling == false)
			        				bomb2.isFalling = true;
		        	    		break;
		        	    	}
		        		}
	        		}
	        	}
	        } );
	      
	    } // end of GamePlate constructor
	    
	    // Method that resets weapon and kills.
	    public void reset(){
	    	if(goToNext){
	    		goToNext = false;
    			game = new GamePlate();
    		}
	    	me = null;  
	    	bomb = null;
	    	bomb2 = null;
	    	target = null;
	    	tbomb = null;
	    	
	    	totWeapon = 0;
	    	for(int i=0; i<WT[levelNum-1]; i++){
	    		numWeapon[i] = WN[i][levelNum-1];
	    		totWeapon += numWeapon[i];
	    	}
	    	numKill = 0;
	    	if(me != null){
	    		me.myHP = HP[levelNum-1];
	    	}
	    }
	    
	   
	    // Method that draws the current state of the game.
	    public void paintComponent(Graphics g) {
	
	    	g.drawImage(bg, 0, 0, null);
	      
	    	if (me == null) {
	    		width = getWidth();
	    		height = getHeight();
	    		me = new Me();
	    		target = new Target();
	    		bomb = new Bomb(); 
	    		if(WT[levelNum-1] >= 2){
	    			bomb2 = new Bomb2();
	    		}
	    		switch (TT[levelNum-1]){
		        case 1:
		        	break;
		        case 2:
		        	tbomb = new TBomb();
		        	break;
		        }
	    	}
	    	
	    	if(!isStart){
	    		g.setColor(Color.RED);
	    		g.setFont(new Font("SansSerif", Font.BOLD, 20));
	    		g.drawString("USE LEFT OR RIGHT ARROW TO MOVE", 20, 30);
	    		g.drawString("USE DOWN ARROW TO DROP BOMB", 20, 60);
	    		g.drawString("CLICK ANYWHERE TO START", 20, 90);
	    	} else if (!isFreeMode) {
	    		if(numKill >= KN[levelNum-1]) {
	    			timer.stop();
	    			isStart = false;
	    			levelNum += 1; // go to next level
	    			if(levelNum > levelMax){
	    				g.setColor(Color.RED);
	    				g.setFont(new Font("SansSerif", Font.BOLD, 90));
	    				g.drawString("Good Job!", 130, 250);
	    				g.drawString("You Win!", 150, 400);
	    				levelNum = 1; 
	    				isWin = true;
	    				return;
	    			} else {
	    				g.setColor(Color.RED);
	    				g.setFont(new Font("SansSerif", Font.BOLD, 90));
	    				g.drawString("Great!", 200, 350);
	    				goToNext = true;
	    				return;
	    			}
	    		} else if ((totWeapon == 0 && numKill < KN[levelNum-1]) || me.myHP <= 0){
	    			timer.stop();
	    			isStart = false;
	    			g.setColor(Color.RED);
	    			g.setFont(new Font("SansSerif", Font.BOLD, 90));
	    			g.drawString("Haha-ha-ha!", 100, 250);
	    			g.drawString("You Loose!", 120, 400);
	    			return;
	    		}
	    	}
	    	
	    	// display current level
	    	if(!isFreeMode){ 
	    		level.setText("LEVEL  " + levelNum);
	    	}
	    	// display killing info
	    	if(!isFreeMode){
	    		kill.setText("Kill: " + numKill + " / " + KN[levelNum-1]);
	    	} else {
	    		kill.setText("Kill: " + numKill);
	    	}
	    	// display HP info
	    	hp.setText("HP: " + me.myHP);
	    	// display weapon info
	    	String weaponStr = "<html>";
	    	for(int i=0; i<WT[levelNum-1]; i++){
	    		weaponStr += WS[i] + ": " + numWeapon[i] + "<br>";
	    	}
	    	weaponStr += "<html>";
	    	weapon.setText(weaponStr); 

	    	me.draw(g);
	    	target.draw(g);
	    	switch(wt){
	    	case 0:
	    		bomb.draw(g);
	    		break;
	    	case 1:
	    		bomb2.draw(g);
	    		break;
	    	}
	    	if(tbomb != null){
	    		tbomb.draw(g);
	    	}
	   }
	   
	   
	   // nested class of Me
	   private class Me {
	      int centerX, centerY;  // Current position of the center of me
	      int myHP;
	      boolean isHit;
	      BufferedImage m = null;
	      
	      Me() { 
	         centerX = width/2;
	         centerY = 105;
	         myHP = HP[levelNum-1];
	         isHit = false;
	         try {
	        	 m = ImageIO.read(new File("me.png"));
	         } catch (IOException e) {}
	      }
	      
	      void updateForNewFrame() {
	         if (centerX < 0)
	            centerX = 0;
	         else if (centerX > width)
	            centerX = width;
	      }
	      
	      void draw(Graphics g) {
	    	  g.drawImage(m, centerX-15, centerY-25, 30, 50, null);
	      }
	   } // end of class Me
	   
	   // nested class of Bomb
	   private class Bomb {
	      int centerX, centerY; // Current position of the center of bomb
	      boolean isFalling;  
	      int dyingFrameNumber;  // the number of frames since been hit
	      BufferedImage b = null;
	      
	      Bomb() {
	          isFalling = false;
	          try {
	        	  b = ImageIO.read(new File("bomb.png"));
	          } catch (IOException e) {}
	      }
	      
	      void updateForNewFrame() {  	    	  
	    	  if (isFalling) {
	    		  if(!target.isHit){
		    		  if (centerY > height) { // bomb drops out of the game frame
		    			  numWeapon[wt] -= 1; // number of weapon left decreases by 1
		    			  totWeapon -= 1; 
		    			  isFalling = false;
		    		  } else if (Math.abs(centerX - target.centerX) <= 36 &&
		                         Math.abs(centerY - target.centerY) <= 21) {
		    			  		 // bomb hits the target
		    			  target.isHit = true;
		    			  dyingFrameNumber = 1; 
		    		  } else { // bomb is falling between me and the target
		    			  centerY += 10;
		    		  }
	    		  } else { // target is hit
	    			  dyingFrameNumber++;
	    			  if (dyingFrameNumber == 15) { 
	    				  numWeapon[wt] -= 1; // number of weapon left decreases by 1
		    			  totWeapon -= 1;
	    				  numKill += 1; // number of killing performed increases by 1
	    				  target.isHit = false; 
	    				  isFalling = false;
	    			  }  
	    		  }
	    	  } else { // bomb has not been thrown out
	    		  centerX = me.centerX + 16;
		          centerY = me.centerY + 16;
	    	  }
	      }
	      
	      void draw(Graphics g) {
	         g.drawImage(b, centerX-7, centerY-12, 14, 24, null);
	         
	         if (target.isHit) {
	        	 if(numKill == KN[levelNum-1]-1){
	        		 g.setColor(Color.YELLOW);
	        		 g.fillOval(target.centerX - 4*dyingFrameNumber,
	        				 	target.centerY - 2*dyingFrameNumber,
	        				 	8*dyingFrameNumber,
	        				 	4*dyingFrameNumber);
	        		 g.setColor(Color.RED);
	        		 g.fillOval(target.centerX - 2*dyingFrameNumber,
	            		 		target.centerY - dyingFrameNumber/2,
	            		 		4*dyingFrameNumber,
	            		 		dyingFrameNumber);
	        	 } else {
	        		 g.setColor(Color.YELLOW);
	        		 g.fillOval(centerX - 2*dyingFrameNumber,
	        				 	centerY - 2*dyingFrameNumber,
	        				 	4*dyingFrameNumber,
	        				 	4*dyingFrameNumber);
	        		 g.setColor(Color.RED);
	        		 g.fillOval(centerX - dyingFrameNumber,
	            		 		centerY - dyingFrameNumber,
	            		 		2*dyingFrameNumber,
	            		 		2*dyingFrameNumber);
	        	 } 
	         }
	      }
	   } // end of class Bomb
	   
	   // nested class of Bomb2
	   private class Bomb2 {
	      int centerX, centerY; // Current position of the center of bomb
	      boolean isFalling;  
	      int dyingFrameNumber;  // the number of frames since been hit
	      BufferedImage b2set = null;
	      BufferedImage b2 = null;
	      
	      Bomb2() {
	          isFalling = false;
	          try {
	        	  b2set = ImageIO.read(new File("bomb2set.png"));
	        	  b2 = ImageIO.read(new File("bomb2.png"));
	          } catch (IOException e) {}
	      }
	      
	      void updateForNewFrame() {  	    	  
	    	  if (isFalling) {
	    		  if(!target.isHit){
		    		  if (centerY > height) { // bomb drops out of the game frame
		    			  numWeapon[wt] -= 1; // number of weapon left decreases by 1
		    			  totWeapon -= 1;
		    			  isFalling = false;
		    		  } else if (Math.abs(centerX - target.centerX) <= 45 &&
		                         Math.abs(centerY - target.centerY) <= 21) {
		    			  		 // bomb hits the target
		    			  target.isHit = true;
		    			  dyingFrameNumber = 1; 
		    		  } else { // bomb is falling between me and the target
		    			  centerY += 20;
		    		  }
	    		  } else { // target is hit
	    			  dyingFrameNumber++;
	    			  if (dyingFrameNumber == 15) { 
		    			  numWeapon[wt] -= 1; // number of weapon left decreases by 1
		    			  totWeapon -= 1;
	    				  numKill += 1; // number of killing performed increases by 1
	    				  target.isHit = false;
	    				  isFalling = false;
	    			  }  
	    		  }
	    	  } else { // bomb has not been thrown out
	    		  centerX = me.centerX + 16;
		          centerY = me.centerY + 16;
	    	  }
	      }
	      
	      void draw(Graphics g) {
	    	  g.drawImage(b2set, centerX-40, centerY-20, 20, 40, null);
	    	  if(isFalling){
	    		  g.drawImage(b2, centerX-50, centerY-20, 40, 50, null);
	    	  } 
	    	  
	    	  if (target.isHit) {
	    		  if(numKill == KN[levelNum-1]-1){
	    			  g.setColor(Color.YELLOW);
	    			  g.fillOval(target.centerX - 4*dyingFrameNumber,
	        				 	target.centerY - 2*dyingFrameNumber,
	        				 	8*dyingFrameNumber,
	        				 	4*dyingFrameNumber);
	    			  g.setColor(Color.RED);
	    			  g.fillOval(target.centerX - 2*dyingFrameNumber,
	            		 		target.centerY - dyingFrameNumber/2,
	            		 		4*dyingFrameNumber,
	            		 		dyingFrameNumber);
	    		  } else {
	    			  g.setColor(Color.YELLOW);
	    			  g.fillOval(centerX - 2*dyingFrameNumber,
	        				 	centerY - 2*dyingFrameNumber,
	        				 	4*dyingFrameNumber,
	        				 	4*dyingFrameNumber);
	    			  g.setColor(Color.RED);
	    			  g.fillOval(centerX - dyingFrameNumber,
	            		 		centerY - dyingFrameNumber,
	            		 		2*dyingFrameNumber,
	            		 		2*dyingFrameNumber);
	    		  } 
	         }
	      }
	   } // end of class Bomb2
	   
	   // nested class of Target
	   private class Target {
	      int centerX, centerY; // Current position of the center of target
	      boolean isMovingLeft;
	      boolean isHit;
	      BufferedImage targ = null;
	      
	      Target() {
	         centerX = (int)(width*Math.random());
	         centerY = height - 40;
	         isHit = false;
	         isMovingLeft = (Math.random() < 0.5);
	         try {
	        	 targ = ImageIO.read(new File("target.png"));
	         } catch (IOException e) {}
	      }
	      
	      void updateForNewFrame() {
	         if (isHit) { 
	            if(bomb.dyingFrameNumber == 15) { // target is killed, reset the target
	                centerX = (int)(width*Math.random());
	                centerY = height - 40;
	                isMovingLeft = (Math.random() < 0.5);
	            } 
	            if(bomb2 != null && bomb2.dyingFrameNumber == 15){
	            	centerX = (int)(width*Math.random());
	                centerY = height - 40;
	                isMovingLeft = (Math.random() < 0.5);
	            }
	         } else { // target is not hit
	        	 
	        	 if (tbomb != null && !tbomb.isShoot){
	        		 if(Math.random()<0.1){
	        			 // probability of shooting is 0.1
	        			 tbomb.isShoot = true;
	        		 }
	        	 }
	        	 if (Math.random() < 0.01) {  
	                  // probability of changing direction is 0.01
	        		 isMovingLeft = ! isMovingLeft; 
	        	 }
	        	 if (isMovingLeft) {
	        		 centerX -= 5*TM[levelNum-1];  
	        		 if (centerX <= 0) { // left periodic boundary condition
	        			 centerX = width;  
	        		 }
	        	 } else {
	        		 centerX += 5*TM[levelNum-1];         
	              	if (centerX > width) { // right periodic boundary condition
	              		centerX = 0;    
	              	}	
	        	 }
	         }
	      }
	      
	      void draw(Graphics g) {
	    		  g.drawImage(targ, centerX-15, centerY-25, 30, 50, null);
	    		  g.setColor(Color.BLACK);
	    		  g.setFont(new Font("SansSerif", Font.BOLD, 18));
	    		  g.drawString(nameStr, centerX+25, centerY-15);
	      }
	   } // end of class Target
	   
	   // nested class of TBomb
	   private class TBomb {
		   int centerX, centerY; // Current position of the center of target's bomb
		   boolean isShoot;  
		   int dyingFrameNumber;  // the number of frames since been hit
		   BufferedImage tb = null;
	      
		   TBomb() {
			   isShoot = false;
			   try {
				   tb = ImageIO.read(new File("tbomb.png"));
			   } catch (IOException e) {}
		   }
	      
		   void updateForNewFrame() {  	    	  
			   if (isShoot) {
				   if(!me.isHit){
					   if (centerY < 0) { // target's bomb shoots out of the game frame
						   isShoot = false;
					   } else if (Math.abs(centerX - me.centerX) <= 36 &&
		                         	Math.abs(centerY - me.centerY) <= 21) {
		    			  		 // tagert's bomb hits me
						   me.isHit = true;
						   dyingFrameNumber = 1;
					   } else { // bomb is flying between me and the target
						   centerY -= 20;
					   }
				   } else { // me is hit
					   dyingFrameNumber++;
					   if (dyingFrameNumber == 15) { 
						   me.myHP -= 1; // HP decreases by 1
						   me.isHit = false; 
						   isShoot = false;
					   }  
				   }
			   } else { // bomb has not been thrown out
				   centerX = target.centerX + 16;
				   centerY = target.centerY + 16;
			   }
		   }
	      
		   void draw(Graphics g) {
			   g.drawImage(tb, centerX-7, centerY-12, 14, 24, null);
	         
			   if (me.isHit) {
				   if(me.myHP == 1){
					   g.setColor(Color.YELLOW);
					   g.fillOval(me.centerX - 4*dyingFrameNumber,
	        				 	me.centerY - 2*dyingFrameNumber,
	        				 	8*dyingFrameNumber,
	        				 	4*dyingFrameNumber);
					   g.setColor(Color.RED);
					   g.fillOval(me.centerX - 2*dyingFrameNumber,
	            		 		me.centerY - dyingFrameNumber/2,
	            		 		4*dyingFrameNumber,
	            		 		dyingFrameNumber);
				   } else {
					   g.setColor(Color.YELLOW);
					   g.fillOval(centerX - 2*dyingFrameNumber,
	        				 	centerY - 2*dyingFrameNumber,
	        				 	4*dyingFrameNumber,
	        				 	4*dyingFrameNumber);
					   g.setColor(Color.RED);
					   g.fillOval(centerX - dyingFrameNumber,
	            		 		centerY - dyingFrameNumber,
	            		 		2*dyingFrameNumber,
	            		 		2*dyingFrameNumber);
	        	 	} 
			   }
		   }
	   } // end of class TBomb

	   // Action of the "New Game" and "Pause" button
	   public void actionPerformed(ActionEvent evt) {
		   String command = evt.getActionCommand();
		   if(command.equals("New Game")){
			   isWin = false;
			   isStart = false;
			   reset();
			   timer.stop();
			   repaint();
		   } else if (command.equals("Pause")){
			   timer.stop();
			   pauseButton.setText("Resume");
			   pauseButton.setActionCommand("Resume");
			   repaint(); 
		   } else if (command.equals("Resume")){
			   timer.start();
			   pauseButton.setText("Pause");
			   pauseButton.setActionCommand("Pause");
			   repaint();
		   } else if (command.equals("Game Mode")){
			   numKill = 0;
			   switch(gameMode.getSelectedIndex()){
			   case 0:
				   level.setText("LEVEL  " + levelNum);
				   isFreeMode = false;
				   reset();
				   break;
			   case 1:
				   level.setText("Free Mode");
				   isFreeMode = true;
				   for(int i=0; i<WT[levelNum-1]; i++){
					   numWeapon[i] = 9999;
				   }
			   }
		   } else {
			    switch(weaponType.getSelectedIndex()){
			    case 0:
				   wt = 0;
				   timer.stop();
				   repaint();
				   break;
			    case 1:
				   wt = 1;
				   timer.stop();
				   repaint();
				   break;
			    }
		   }
	   }
   
	} // end of class GamePlate
   
} // end of class GPanel
