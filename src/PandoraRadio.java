// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   PandoraRadio.java

import java.util.ArrayList;

public interface PandoraRadio
{

    public abstract String pandoraEncrypt(String s);

    public abstract String pandoraDecrypt(String s);

    public abstract void connect(String s, String s1);

    public abstract void sync();

    public abstract void disconnect();

    public abstract ArrayList getStations();

    public abstract Station getStationById(long l);

    public abstract Song[] getPlaylist(Station station);

    public abstract boolean rate(Station station, Song song, boolean flag);

    public abstract boolean bookmarkSong(Station station, Song song);

    public abstract boolean isAlive();

    public abstract boolean bookmarkArtist(Station station, Song song);

    public abstract boolean tired(Station station, Song song);
}
