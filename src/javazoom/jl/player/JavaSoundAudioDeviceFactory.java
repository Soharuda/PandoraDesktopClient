// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 

package javazoom.jl.player;

import javazoom.jl.decoder.JavaLayerException;

// Referenced classes of package javazoom.jl.player:
//            AudioDeviceFactory, JavaSoundAudioDevice, AudioDevice

public class JavaSoundAudioDeviceFactory extends AudioDeviceFactory
{

    public JavaSoundAudioDeviceFactory()
    {
        tested = false;
    }

    public synchronized AudioDevice createAudioDevice()
        throws JavaLayerException
    {
		try {
			if(!tested)
			{
				testAudioDevice();
				tested = true;
			}
			return createAudioDeviceImpl();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
    }
	
	public JavaSoundAudioDevice JSAD;
	
	public JavaSoundAudioDevice getJSAD() {
		return JSAD;
	}

    protected JavaSoundAudioDevice createAudioDeviceImpl()
        throws JavaLayerException
    {
		try {
			ClassLoader classloader = getClass().getClassLoader();
			JavaSoundAudioDevice javasoundaudiodevice = (JavaSoundAudioDevice)instantiate(classloader, "javazoom.jl.player.JavaSoundAudioDevice");
			JSAD = javasoundaudiodevice;
			return javasoundaudiodevice;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
    }

    public void testAudioDevice()
        throws JavaLayerException
    {
        JavaSoundAudioDevice javasoundaudiodevice = createAudioDeviceImpl();
        javasoundaudiodevice.test();
    }

    private boolean tested;
    private static final String DEVICE_CLASS_NAME = "javazoom.jl.player.JavaSoundAudioDevice";
}
