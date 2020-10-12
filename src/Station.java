// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   Station.java

import java.io.Serializable;
import java.util.HashMap;
import org.xmlrpc.android.XMLRPCException;

public class Station
    implements Comparable, Serializable
{

    public Station(HashMap d, PandoraRadio instance)
    {
        id = (String)d.get("stationId");
        idToken = (String)d.get("stationIdToken");
        isQuickMix = ((Boolean)d.get("isQuickMix")).booleanValue();
        name = (String)d.get("stationName");
        pandora = instance;
        useQuickMix = false;
    }

    public Song[] getPlaylist(boolean forceDownload)
        throws XMLRPCException
    {
        return getPlaylist("aacplus", forceDownload);
    }

    public Song[] getPlaylist(String format, boolean forceDownload)
        throws XMLRPCException
    {
        if(forceDownload || currentPlaylist == null)
            return getPlaylist();
        else
            return currentPlaylist;
    }

    public Song[] getPlaylist(String format)
    {
        return getPlaylist();
    }

    public Song[] getPlaylist()
    {
        Song song_array[] = pandora.getPlaylist(this);
        if(song_array.length == 0)
            pandora.disconnect();
        return song_array;
    }

    public long getId()
    {
        try
        {
            return Long.parseLong(id);
        }
        catch(NumberFormatException ex)
        {
            return (long)id.hashCode();
        }
    }

    public String getName()
    {
        return name;
    }

    public String getStationImageUrl()
        throws XMLRPCException
    {
        getPlaylist(false);
        return currentPlaylist[0].getAlbumCoverUrl();
    }

    public int compareTo(Station another)
    {
        return getName().compareTo(another.getName());
    }

    public boolean equals(Station another)
    {
        return getName().equals(another.getName());
    }

    public String getStationId()
    {
        return id;
    }

    public String getStationIdToken()
    {
        return idToken;
    }

    public String toString()
    {
        return name;
    }

    public boolean isQuickMix()
    {
        return isQuickMix;
    }

    public int compareTo(Object obj)
    {
        return compareTo((Station)obj);
    }

    private static final long serialVersionUID = 1L;
    private String id;
    private String idToken;
    private boolean isQuickMix;
    private String name;
    private transient Song currentPlaylist[];
    private transient boolean useQuickMix;
    private transient PandoraRadio pandora;
}
