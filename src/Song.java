// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   Song.java


public class Song
{

    public Song(String album, String artist, String audioUrl, String title, String albumDetailUrl, String artRadio, String trackToken, 
            Integer rating, String stationId)
    {
        this.album = album;
        this.artist = artist;
        this.audioUrl = audioUrl;
        this.title = title;
        this.albumDetailUrl = albumDetailUrl;
        this.artRadio = artRadio;
        this.trackToken = trackToken;
        this.rating = rating;
        this.stationId = stationId;
        playlistTime = System.currentTimeMillis() / 1000L;
    }

    public String getTrackToken()
    {
        return trackToken;
    }

    public void setTrackToken(String trackToken)
    {
        this.trackToken = trackToken;
    }

    public void setAlbum(String album)
    {
        this.album = album;
    }

    public void setArtist(String artist)
    {
        this.artist = artist;
    }

    public void setTitle(String title)
    {
        this.title = title;
    }

    public boolean isStillValid()
    {
        return System.currentTimeMillis() / 1000L - playlistTime < 10800L;
    }

    public boolean isLoved()
    {
        return rating.intValue() == 1;
    }
	
	public void setRating(int i) {
		rating = i;
	}

    public String getAudioUrl()
    {
        return audioUrl;
    }

    public String getAlbumCoverUrl()
    {
        return artRadio;
    }

    public String getTitle()
    {
        return title;
    }

    public String getArtist()
    {
        return artist;
    }

    public String getAlbum()
    {
        return album;
    }

    private String album;
    private String artist;
    private String audioUrl;
    private String title;
    private String albumDetailUrl;
    private String artRadio;
    private String trackToken;
    private Integer rating;
    private String stationId;
    private boolean tired;
    private String message;
    private Object startTime;
    private boolean finished;
    private long playlistTime;
    private PandoraRadio pandora;
}
