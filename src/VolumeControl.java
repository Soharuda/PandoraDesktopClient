// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   VolumeControl.java

import java.awt.Dimension;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Hashtable;
import javax.imageio.ImageIO;
import javax.sound.sampled.*;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.sound.sampled.Mixer.*;

public class VolumeControl extends JSlider
{

    public VolumeControl()
    {
        super(0, 1000);
        control = getControl();
        if(control == null)
            control = getGainControl();
        if(control != null)
        {
            minVol = control.getMinimum();
            maxVol = control.getMaximum();
            volumeRange = maxVol - minVol;
            initialize();
        } else
        {
            setVisible(false);
            System.out.println("Hidding vol controls...");
            minVol = maxVol = volumeRange = -1F;
        }
    }

    private void initialize()
    {
        setPreferredSize(new Dimension(133, 50));
        float currentVolume = control.getValue();
        int sliderVal = (int)(((currentVolume - minVol) / volumeRange) * 1000F);
        setValue(sliderVal);
        setLabels();
        addListeners();
    }

    private void addListeners()
    {
        addChangeListener(new ChangeListener() {

            public void stateChanged(ChangeEvent e)
            {
                int sliderValue = getValue();
                float f = minVol + ((float)sliderValue * volumeRange) / 1000F;
                control.setValue(f);
            }
        });
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

    private void setLabels()
    {
        Image min = scale("sound-low.png", 23, 23);
        Image max = scale("sound-high.png", 18, 18);
        JLabel sndMin = new JLabel(new ImageIcon(min));
        JLabel sndMax = new JLabel(new ImageIcon(max));
        Hashtable labels = new Hashtable(3);
        labels.put(new Integer(0), sndMin);
        labels.put(new Integer(1000), sndMax);
        setLabelTable(labels);
        setPaintLabels(true);
    }

    private Image scale(String filename, int w, int h)
    {
        BufferedImage bi = null;
        try
        {
            bi = ImageIO.read(getResource(filename));
        }
        catch(IOException e)
        {
            e.printStackTrace();
            return null;
        }
        return bi.getScaledInstance(w, h, 1);
    }
	
	public static FloatControl getGainControl() {
		Info info[] = AudioSystem.getMixerInfo();
		Mixer mixer;
		Port port;
		FloatControl control;
		try {
			for (int i = 0; i < info.length; i++) {
				mixer = AudioSystem.getMixer(info[i]);
				if (mixer.isLineSupported(javax.sound.sampled.Port.Info.SPEAKER)) {
					port = (Port)mixer.getLine(javax.sound.sampled.Port.Info.SPEAKER);
					control = (FloatControl) port.getControl(FloatControl.Type.MASTER_GAIN);
					return control;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return null;
	}
	
	public static FloatControl getControl() {
		Info info[] = AudioSystem.getMixerInfo();
		Mixer mixer;
		Port port;
		FloatControl control;
		try {
			for (int i = 0; i < info.length; i++) {
				mixer = AudioSystem.getMixer(info[i]);
				if (mixer.isLineSupported(javax.sound.sampled.Port.Info.SPEAKER)) {
					port = (Port)mixer.getLine(javax.sound.sampled.Port.Info.SPEAKER);
					control = (FloatControl) port.getControl(FloatControl.Type.VOLUME);
					return control;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return null;
	}

    /*public static FloatControl getGainControl()
    {
        javax.sound.sampled.Mixer.Info infos[] = AudioSystem.getMixerInfo();
        javax.sound.sampled.Mixer.Info ainfo[];
        int j = (ainfo = infos).length;
		System.out.println("1");
        for(int i = 0; i < j; i++)
        {
            javax.sound.sampled.Mixer.Info info = ainfo[i];
            Mixer mixer = AudioSystem.getMixer(info);
			System.out.println("1");
            if(!mixer.isLineSupported(javax.sound.sampled.Port.Info.SPEAKER))
                continue;
            Port port;
			System.out.println("2");
            try
            {
                port = (Port)mixer.getLine(javax.sound.sampled.Port.Info.SPEAKER);
                port.open();
				System.out.println("3");
            }
            catch(LineUnavailableException e)
            {
                e.printStackTrace();
				System.out.println("4");
                continue;
            }
            if(port.isControlSupported(javax.sound.sampled.FloatControl.Type.MASTER_GAIN))
                return (FloatControl)port.getControl(javax.sound.sampled.FloatControl.Type.VOLUME);
			System.out.println("5");
            port.close();
        }
		System.out.println("6");
        return null;
    }

    public static FloatControl getControl()
    {
        javax.sound.sampled.Mixer.Info infos[] = AudioSystem.getMixerInfo();
        javax.sound.sampled.Mixer.Info ainfo[];
        int j = (ainfo = infos).length;
        for(int i = 0; i < j; i++)
        {
            javax.sound.sampled.Mixer.Info info = ainfo[i];
            Mixer mixer = AudioSystem.getMixer(info);
            if(!mixer.isLineSupported(javax.sound.sampled.Port.Info.SPEAKER))
                continue;
            Port port;
            try
            {
                port = (Port)mixer.getLine(javax.sound.sampled.Port.Info.SPEAKER);
                port.open();
            }
            catch(LineUnavailableException e)
            {
                e.printStackTrace();
                continue;
            }
            if(port.isControlSupported(javax.sound.sampled.FloatControl.Type.VOLUME))
            {
                FloatControl volume = (FloatControl)port.getControl(javax.sound.sampled.FloatControl.Type.VOLUME);
                System.out.println(info);
                System.out.println((new StringBuilder("- ")).append(javax.sound.sampled.Port.Info.SPEAKER).toString());
                System.out.println((new StringBuilder("  - ")).append(volume).toString());
                Float f = new Float(0.10000000000000001D);
                return volume;
            }
            port.close();
        }

        return null;
    }*/

    private static final long serialVersionUID = 0xb80699fc81a722deL;
    public static final int SIZE = 1000;
    public final float minVol;
    public final float maxVol;
    public final float volumeRange;
    private FloatControl control;

}
