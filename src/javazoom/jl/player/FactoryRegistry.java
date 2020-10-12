// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 

package javazoom.jl.player;

import java.util.Enumeration;
import java.util.Hashtable;
import javazoom.jl.decoder.JavaLayerException;

// Referenced classes of package javazoom.jl.player:
//            AudioDeviceFactory, JavaSoundAudioDeviceFactory, AudioDevice

public class FactoryRegistry extends AudioDeviceFactory
{

    public FactoryRegistry()
    {
        factories = new Hashtable();
    }

    public static synchronized FactoryRegistry systemRegistry()
    {
        if(instance == null)
        {
            instance = new FactoryRegistry();
            instance.registerDefaultFactories();
        }
        return instance;
    }

    public void addFactory(AudioDeviceFactory audiodevicefactory)
    {
        factories.put(audiodevicefactory.getClass(), audiodevicefactory);
    }

    public void removeFactoryType(Class class1)
    {
        factories.remove(class1);
    }

    public void removeFactory(AudioDeviceFactory audiodevicefactory)
    {
        factories.remove(audiodevicefactory.getClass());
    }

    public AudioDevice createAudioDevice()
        throws JavaLayerException
    {
        AudioDevice audiodevice = null;
        AudioDeviceFactory aaudiodevicefactory[] = getFactoriesPriority();
        if(aaudiodevicefactory == null)
            throw new JavaLayerException(this + ": no factories registered");
        Object obj = null;
        for(int i = 0; audiodevice == null && i < aaudiodevicefactory.length; i++)
            try
            {
                audiodevice = aaudiodevicefactory[i].createAudioDevice();
            }
            catch(JavaLayerException javalayerexception)
            {
                obj = javalayerexception;
            }

        if(audiodevice == null && obj != null) {
            throw new JavaLayerException("Cannot create AudioDevice", ((Throwable) (obj)));
        } else {
			System.out.println("JSAD Set");
			jsad = (JavaSoundAudioDevice) audiodevice;
            return audiodevice;
		}
    }

    protected AudioDeviceFactory[] getFactoriesPriority()
    {
        AudioDeviceFactory aaudiodevicefactory[] = null;
        synchronized(factories)
        {
            int i = factories.size();
            if(i != 0)
            {
                aaudiodevicefactory = new AudioDeviceFactory[i];
				
                int j = 0;
                for(Enumeration enumeration = factories.elements(); enumeration.hasMoreElements();)
                {
                    AudioDeviceFactory audiodevicefactory = (AudioDeviceFactory)enumeration.nextElement();
                    aaudiodevicefactory[j++] = audiodevicefactory;
                }

            }
        }
        return aaudiodevicefactory;
    }

    protected void registerDefaultFactories()
    {
		addFactory(new JavaSoundAudioDeviceFactory());
		//jsadf = new JavaSoundAudioDeviceFactory();
        //addFactory(jsadf);
    }
	
	public JavaSoundAudioDevice getJSAD() {
		if (jsad != null) {
			return jsad;
		}
		return null;
	}
	
	public JavaSoundAudioDevice jsad;
    private static FactoryRegistry instance = null;
    protected Hashtable factories;

}
