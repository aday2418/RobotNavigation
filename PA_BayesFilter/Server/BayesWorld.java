
import javax.swing.*;
import java.awt.event.*;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.lang.*;
import javax.swing.JComponent;
import javax.swing.JFrame;
import java.io.*;
import java.util.Random;

import java.net.*;




class MyCanvas extends JComponent {
    int winWidth, winHeight;
    double sqrWdth, sqrHght;
    Color gris = new Color(170,170,170);
    Color myWhite = new Color(220, 220, 220);
    
    int xpos, ypos;

    World mundo;
    
    public MyCanvas(int w, int h, World wld, int _x, int _y) {
        mundo = wld;
        winWidth = w;
        winHeight = h;
        updatePosition(_x, _y);
        
        sqrWdth = (double)w / mundo.width;
        sqrHght = (double)h / mundo.height;
    }
    
    public void updatePosition(int _x, int _y) {
        xpos = _x;
        ypos = _y;
        
        repaint();
    }
    
    public void paint(Graphics g) {
        for (int y = 0; y < mundo.height; y++) {
            for (int x = 0; x < mundo.width; x++) {
                if (mundo.grid[x][y] == 1) {
                    g.setColor(Color.black);
                    g.fillRect((int)(x * sqrWdth), (int)(y * sqrHght), (int)sqrWdth, (int)sqrHght);
                }
                else if (mundo.grid[x][y] == 0) {
                    g.setColor(myWhite);
                    g.fillRect((int)(x * sqrWdth), (int)(y * sqrHght), (int)sqrWdth, (int)sqrHght);
                }
                else if (mundo.grid[x][y] == 2) {
                    g.setColor(Color.red);
                    g.fillRect((int)(x * sqrWdth), (int)(y * sqrHght), (int)sqrWdth, (int)sqrHght);
                }
                else if (mundo.grid[x][y] == 3) {
                    g.setColor(Color.green);
                    g.fillRect((int)(x * sqrWdth), (int)(y * sqrHght), (int)sqrWdth, (int)sqrHght);
                }
            }
            if (y != 0) {
                g.setColor(gris);
                g.drawLine(0, (int)(y * sqrHght), (int)winWidth, (int)(y * sqrHght));
            }
        }
        for (int x = 0; x < mundo.width; x++) {
                g.setColor(gris);
                g.drawLine((int)(x * sqrWdth), 0, (int)(x * sqrWdth), (int)winHeight);
        }
        
        g.setColor(Color.blue);
        g.fillOval((int)(xpos * sqrWdth)+1, (int)(ypos * sqrHght)+1, (int)(sqrWdth-1.4), (int)(sqrHght-1.4));
    }
}

public class BayesWorld extends JFrame {
    public static final int NORTH = 0;
    public static final int SOUTH = 1;
    public static final int EAST = 2;
    public static final int WEST = 3;
    public static final int STAY = 4;

    Color bkgroundColor = new Color(230,230,230);
    static MyCanvas canvas;
    World mundo;
    int xpos, ypos;
    double moveProb, sensorAccuracy;
    
    ServerSocket serverSocket;
    Socket clientSocket;
    PrintWriter sout;
    BufferedReader sin;
    
    Random rand;
    
    public BayesWorld(String fnombre, double _moveProb, double _sensorAccuracy, String _known) {
        rand = new Random();
    
        mundo = new World(fnombre);
        int width = 500;
        int height = 500;
        moveProb = _moveProb;
        sensorAccuracy = _sensorAccuracy;
        
        initRobotPosition();
    
        int bar = 20;
        setSize(width,height+bar);
        getContentPane().setBackground(bkgroundColor);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setBounds(0, 0, width, height+bar);
        canvas = new MyCanvas(width, height, mundo, xpos, ypos);
        getContentPane().add(canvas);
        
        setVisible(true);
        setTitle("BayesWorld");
        
        getConnection(3333, fnombre, _known);
        survive();
    }
    
    private void getConnection(int port, String fnombre, String _known) {
        System.out.println("Set up the connection:" + port);
        
        try {
            serverSocket = new ServerSocket(port);
            clientSocket = serverSocket.accept();
            sout = new PrintWriter(clientSocket.getOutputStream(), true);
            sin = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
    
            System.out.println("Connection established.");
        
            sout.println(fnombre);
            sout.println(moveProb);
            sout.println(sensorAccuracy);
            
            if (_known.equals("known")) {
                sout.println("known");
                sout.println(xpos);
                sout.println(ypos);
            }
            else {
                sout.println("unknown");
            }
        } catch (IOException e) {
            System.err.println("Caught IOException: " + e.getMessage());
        }
    }
    
    void initRobotPosition() {
        while (true) {
            // random initial position
            //xpos = rand.nextInt(mundo.width);
            //ypos = rand.nextInt(mundo.height);
            
            // random initial position in bottom right quadrant
            xpos = rand.nextInt(mundo.width / 2) + (mundo.width/2);
            ypos = rand.nextInt(mundo.height / 2) + (mundo.height/2);
    
            if (mundo.grid[xpos][ypos] == 0)
                break;
        }
    }
    
    void moveIt(int action) {
        int oldx = xpos, oldy = ypos;
        
        switch (action) {
            case NORTH: // up
                ypos --;
                break;
            case SOUTH:
                ypos ++;
                break;
            case WEST:
                xpos --;
                break;
            case EAST: //
                xpos ++;
                break;
            case 4: // stay
                break;
        }
        
        if (mundo.grid[xpos][ypos] == 1) {
            xpos = oldx;
            ypos = oldy;
        }
        canvas.updatePosition(xpos, ypos);
    }
    
    void moveRobot(int action) {
        double value = rand.nextInt(1001) / 1001.0;
        
        if (value <= moveProb)
            moveIt(action);
        else { // pick a different move randomly
            int other = rand.nextInt(5);
            while (other == action)
                other = rand.nextInt(5);
            moveIt(other);
        }
    }
    
    // returns a strong with a char specifying north south east west; 1 = wall; 0 = no wall
    String getSonarReadings() {
        double value = rand.nextInt(1001) / 1001.0;
        String reading = "";
        // north
        if (mundo.grid[xpos][ypos-1] == 1) { // it is a wall
            if (value <= sensorAccuracy)
                reading += "1";
            else
                reading += "0";
        }
        else { // it is not a wall
            if (value <= sensorAccuracy)
                reading += "0";
            else
                reading += "1";
        }
        // south
        value = rand.nextInt(1001) / 1001.0;
        if (mundo.grid[xpos][ypos+1] == 1) { // it is a wall
            if (value <= sensorAccuracy)
                reading += "1";
            else
                reading += "0";
        }
        else { // it is not a wall
            if (value <= sensorAccuracy)
                reading += "0";
            else
                reading += "1";
        }
        // east
        value = rand.nextInt(1001) / 1001.0;
        if (mundo.grid[xpos+1][ypos] == 1) { // it is a wall
            if (value <= sensorAccuracy)
                reading += "1";
            else
                reading += "0";
        }
        else { // it is not a wall
            if (value <= sensorAccuracy)
                reading += "0";
            else
                reading += "1";
        }
        // west
        value = rand.nextInt(1001) / 1001.0;
        if (mundo.grid[xpos-1][ypos] == 1) { // it is a wall
            if (value <= sensorAccuracy)
                reading += "1";
            else
                reading += "0";
        }
        else { // it is not a wall
            if (value <= sensorAccuracy)
                reading += "0";
            else
                reading += "1";
        }
    
        return reading;
    }
    
    void survive() {
        int action;
        boolean theEnd = false;
        int numMoves = 0;
        
        while (true) {
            try {
                action = Integer.parseInt(sin.readLine());
                System.out.println("Move the robot: " + action);
                moveRobot(action);
                
                String sonars = getSonarReadings();
                System.out.println(sonars);
                if (mundo.grid[xpos][ypos] == 3) {
                    System.out.println("Winner");
                    //sout.println("win");
                    sonars += "winner";
                    theEnd = true;
                }
                else if (mundo.grid[xpos][ypos] == 2) {
                    System.out.println("Loser");
                    //sout.println("lose");
                    sonars += "loser";
                    theEnd = true;
                }
                sout.println(sonars);
                
                numMoves++;
                
                if (theEnd)
                    break;
            }
            catch (IOException e) {
                System.out.println(e);
            }
        }
        System.out.println("It took " + numMoves + " moves.");
    }

    public static void main(String[] args) {
        BayesWorld bw = new BayesWorld(args[0], Double.parseDouble(args[1]), Double.parseDouble(args[2]), args[3]);
    }
}