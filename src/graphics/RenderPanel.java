package graphics;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.Dimension;

import javax.swing.JPanel;

/**
 * A modified JPanel in order to allow easy graphics rendering. 
 * All graphics are drawn onto the BufferedImage "surface" through the Graphics2D attribute.
 * Updates are shown when repaint() is called
 */
public class RenderPanel extends JPanel{

    private BufferedImage surface;
    private Graphics2D g2;

    public RenderPanel(int screenWidth, int screenHeight){
        setPreferredSize(new Dimension(screenWidth, screenHeight));
        surface = new BufferedImage(screenWidth, screenHeight, BufferedImage.TYPE_INT_RGB);
        g2 = (Graphics2D) surface.getGraphics(); //Get graphics context
    }

    public void paintComponent(Graphics g){
        g.drawImage(surface, 0, 0, null);
    }

    public Graphics2D getGraphics2D(){
        return g2;
    }
}
