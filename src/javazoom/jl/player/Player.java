// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 

package javazoom.jl.player;

import java.io.InputStream;
import javazoom.jl.decoder.*;

// Referenced classes of package javazoom.jl.player:
//            FactoryRegistry, AudioDevice

public class Player
{

    public Player(InputStream inputstream)
        throws JavaLayerException
    {
        this(inputstream, null);
    }

    public Player(InputStream inputstream, AudioDevice audiodevice)
        throws JavaLayerException
    {
        frame = 0;
        closed = false;
        complete = false;
        lastPosition = 0;
        bitstream = new Bitstream(inputstream);
        decoder = new Decoder();
        if(audiodevice != null)
        {
            audio = audiodevice;
        } else
        {
            factoryregistry = FactoryRegistry.systemRegistry();
            audio = factoryregistry.createAudioDevice();
        }
        audio.open(decoder);
    }

    public void play()
        throws JavaLayerException
    {
        play(0x7fffffff);
    }

    public boolean play(int i)
        throws JavaLayerException
    {
        boolean flag;
        for(flag = true; i-- > 0 && flag; flag = decodeFrame());
        if(!flag)
        {
            AudioDevice audiodevice = audio;
            if(audiodevice != null)
            {
                audiodevice.flush();
                synchronized(this)
                {
                    complete = !closed;
                    close();
                }
            }
        }
        return flag;
    }

    public synchronized void close()
    {
        AudioDevice audiodevice = audio;
        if(audiodevice != null)
        {
            closed = true;
            audio = null;
            audiodevice.close();
            lastPosition = audiodevice.getPosition();
            try
            {
                bitstream.close();
            }
            catch(BitstreamException bitstreamexception) { }
        }
    }

    public synchronized boolean isComplete()
    {
        return complete;
    }

    public int getPosition()
    {
        int i = lastPosition;
        AudioDevice audiodevice = audio;
        if(audiodevice != null)
            i = audiodevice.getPosition();
        return i;
    }
	
	public JavaSoundAudioDevice getJSAD() {
		return factoryregistry.getJSAD();
	}

    protected boolean decodeFrame()
        throws JavaLayerException
    {
        AudioDevice audiodevice = audio;
        if(audiodevice == null)
            return false;
        javazoom.jl.decoder.Header header = bitstream.readFrame();
        if(header == null)
            return false;
        try
        {
            SampleBuffer samplebuffer = (SampleBuffer)decoder.decodeFrame(header, bitstream);
            synchronized(this)
            {
                AudioDevice audiodevice1 = audio;
                if(audiodevice1 != null)
                    audiodevice1.write(samplebuffer.getBuffer(), 0, samplebuffer.getBufferLength());
            }
            bitstream.closeFrame();
        }
        catch(RuntimeException runtimeexception)
        {
            throw new JavaLayerException("Exception decoding audio frame", runtimeexception);
        }
        return true;
    }

    private int frame;
    private Bitstream bitstream;
    private Decoder decoder;
    private AudioDevice audio;
    private boolean closed;
    private boolean complete;
    private int lastPosition;
	private FactoryRegistry factoryregistry;
}
