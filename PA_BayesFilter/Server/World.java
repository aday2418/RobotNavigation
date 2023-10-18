import java.awt.*;
import java.util.*;
import java.awt.event.*;
import java.lang.*;
import java.io.*;
import java.net.*;
import javax.swing.*;
import java.math.*;
import java.text.*;

public class World {
    int width, height;
    int[][] grid;

    World(String worldFile) {
        try {
            FileReader fileReader = new FileReader("../Mundos/" + worldFile);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            
            width = Integer.parseInt(bufferedReader.readLine());
            height = Integer.parseInt(bufferedReader.readLine());
            
            //System.out.println("Width: " + width + "; Height = " + height);
            
            grid = new int[width][height];
            for (int y = 0; y < height; y++) {
                String line = bufferedReader.readLine();
                for (int x = 0; x < width; x++) {
                    if (line.charAt(x) == '0')
                        grid[x][y] = 0;
                    else if (line.charAt(x) == '1')
                        grid[x][y] = 1;
                    else if (line.charAt(x) == '2')
                        grid[x][y] = 2;
                    else if (line.charAt(x) == '3')
                        grid[x][y] = 3;
                }
            }
            
            bufferedReader.close();
            fileReader.close();
        }
        catch(IOException e) {
            System.out.println(e);
        }
    }

}