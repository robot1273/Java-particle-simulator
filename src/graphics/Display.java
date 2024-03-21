package graphics;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.MouseInfo;

import javax.swing.JFrame;


/**
 * Display acts as the main surface to render graphics to. 
 * Once you have rendered the desired graphics, call update() to draw them to the screen
 */
public class Display{
    private int screenWidth;
    private int screenHeight;

    private JFrame display;
    private RenderPanel panel;

    public Display(int screenWidth, int screenHeight){
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;

        display = new JFrame("Window");
        panel = new RenderPanel(screenWidth, screenHeight);

        display.setSize(screenWidth, screenHeight);
        display.setLocationRelativeTo(null);
        display.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        display.setResizable(false);
        
        display.add(panel);
        display.pack(); //Force correct sizing

        display.setVisible(true);

    }

    public void update(){
        panel.repaint();
    }

    public void drawCircle(int x, int y, int radius, Color color){
        Graphics2D g = panel.getGraphics2D();
        g.setColor(color);
        g.fillOval(x - radius, y - radius, radius*2, radius*2);
    }

    public void drawCircle(int x, int y, int radius, Color color, boolean doOutline){
        Graphics2D g = panel.getGraphics2D();
        g.setColor(color);
        if (!doOutline){g.fillOval(x - radius, y - radius, radius*2, radius*2);}
        else{g.drawOval(x - radius, y - radius, radius*2, radius*2);}
    }

    public void drawRect(int x, int y, int w, int h, Color color){
        Graphics2D g = panel.getGraphics2D();
        g.setColor(color);
        g.drawRect(x, y, w, h);
    }

    public void fill(Color color){
        Graphics2D g = panel.getGraphics2D();
        g.setColor(color);
        g.fillRect(0, 0, getWidth(), getHeight());
    }

    public void drawText(String string, int x, int y, Color color){
        Graphics2D g = panel.getGraphics2D();
        g.setColor(color);
        g.drawString(string, x, y);
    }

    public void clear(){
        Graphics2D g = panel.getGraphics2D();
        g.clearRect(0, 0, getWidth(), getHeight());
    }

    public void drawPixel(int x, int y, Color color){
        drawCircle(x, y, 1, color);
    }

    public int getWidth(){
        return this.screenWidth;
    }

    public int getHeight(){
        return this.screenHeight;
    }

    public double getMouseX(){
        return MouseInfo.getPointerInfo().getLocation().getX() - display.getLocationOnScreen().getX() - 8;
    }

    public double getMouseY(){
        return MouseInfo.getPointerInfo().getLocation().getY() - display.getLocationOnScreen().getY() - 31;
    }
    
}
