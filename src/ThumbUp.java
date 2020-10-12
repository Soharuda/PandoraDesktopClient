import java.awt.Dimension;
import java.awt.Image;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import java.io.File;

public class ThumbUp extends JLabel
{

    public ThumbUp()
    {
        setPreferredSize(new Dimension(30, 30));
        initialize();
    }

    private void initialize()
    {
        state = 0;
        ic_selected = new ImageIcon(getScaledImage("thumb_up_filled.png", 25, 25));
        ic_hover = new ImageIcon(getScaledImage("thumb_up_hover.png", 25, 25));
        ic_outline = new ImageIcon(getScaledImage("thumb_up.png", 25, 25));
        updateImage();
        addListeners();
    }

    private void addListeners()
    {
        addMouseListener(new MouseAdapter() {

            public void mouseEntered(MouseEvent e)
            {
                if(state == 0)
                    setState(1);
            }

            public void mouseExited(MouseEvent e)
            {
                if(state == 1)
                    setState(0);
            }
        });
    }

    public void setState(int newState)
    {
        state = newState;
        updateImage();
    }

    public void updateImage()
    {
        switch(state)
        {
        case 1: // '\001'
            setIcon(ic_hover);
            break;

        case 2: // '\002'
            setIcon(ic_selected);
            break;

        case 0: // '\0'
        default:
            setIcon(ic_outline);
            break;
        }
        repaint();
    }

    public void resetToDefault()
    {
        state = 0;
        updateImage();
    }

    private Image getScaledImage(String filename, int w, int h)
    {
        BufferedImage bi = null;
        try
        {
            bi = ImageIO.read(getResource(filename));
			//bi = ImageIO.read(new File(filename));
        }
        catch(IOException e)
        {
            e.printStackTrace();
            return null;
        }
        return bi.getScaledInstance(w, h, 1);
    }

    private URL getResource(String file)
    {
		URL url;
		try {
			url = getClass().getResource(file);
			return url;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
        //URLClassLoader urlLoader = (URLClassLoader)getClass().getClassLoader();
        //return urlLoader.findResource(file);
    }

    public static final int DEFAULT = 0;
    public static final int HOVER = 1;
    public static final int SELECTED = 2;
    private ImageIcon ic_selected;
    private ImageIcon ic_hover;
    private ImageIcon ic_outline;
    private final String path_selected = "thumb_up_filled.png";
    private final String path_hover = "thumb_up_hover.png";
    private final String path_outline = "thumb_up.png";
    private final int SIZE = 30;
    private final int SCALE = 25;
    private int state;

}
