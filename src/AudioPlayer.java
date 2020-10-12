import java.awt.image.BufferedImage;
import java.io.*;
import java.net.*;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.HashMap;
import javax.imageio.ImageIO;
import javax.sound.sampled.*;
import javax.swing.*;
import javazoom.jl.decoder.JavaLayerException;
import javazoom.jl.player.Player;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;
import java.io.File;

public class AudioPlayer
{
    private class SongPlayer extends Thread
    {

        private void setRequestHeaders(HttpURLConnection conn)
        {
            conn.setRequestProperty("User-Agent", "Mozilla/5.0 (compatible; MSIE 9.0; Windows NT 6.1; WOW64; Trident/5.0)");
            conn.setRequestProperty("Accept-Encoding", "gzip, deflate");
            conn.setRequestProperty("Accept", "*/*");
        }

        public void stopPlayBack()
        {
            audioPlayer.isPlaying = false;
            player.close();
            isAlive();
        }

        public void skipSong()
        {
            audioPlayer.isPlaying = true;
            player.close();
        }

        private void trackSong()
        {
            String artist = s.getArtist();
            String album = s.getAlbum();
            String title = s.getTitle();
            SongInfo info = new SongInfo(title, artist, album);
            String station = audioPlayer.getCurrentStation();
            ArrayList songInfoList;
            if(audioPlayer.playedSongs.containsKey(station))
            {
                songInfoList = (ArrayList)audioPlayer.playedSongs.get(station);
            } else
            {
                songInfoList = new ArrayList(40);
                audioPlayer.playedSongs.put(station, songInfoList);
            }
            songInfoList.add(info);
        }

        public void play()
        {
            updateLabels();
            updateUpComingSongLabel();
            updateThumbs();
            start();
			if (autoDownload) {
				saveCurrentSongToDisk();
			}
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
        }

        public void updateThumbs()
        {
            boolean starred = s.isLoved();
            if(starred)
                audioPlayer.pdc.showThumbsUP();
        }

        private void updateLabels()
        {
			if (!usePDC) {
				return;
			}
            String song = s.getTitle();
            String artist = s.getArtist();
            String album = s.getAlbum();
            String nl = "<br/>";
            JLabel img = audioPlayer.getAlbumArt();
            JLabel songInfo = audioPlayer.getSongInfoLabel();
            String songDetails = "<html>";
            songDetails = (new StringBuilder(String.valueOf(songDetails))).append("<u>Station : ").append(audioPlayer.getCurrentStation()).append("</u>").append(nl).toString();
            songDetails = (new StringBuilder(String.valueOf(songDetails))).append("Song : ").append(song).append(nl).toString();
            songDetails = (new StringBuilder(String.valueOf(songDetails))).append("Artist :  ").append(artist).append(nl).toString();
            songDetails = (new StringBuilder(String.valueOf(songDetails))).append("Album : ").append(album).append(nl).toString();
            songDetails = (new StringBuilder(String.valueOf(songDetails))).append("</html>").toString();
            URL albumCover;
            try
            {
                albumCover = new URL(s.getAlbumCoverUrl());
            }
            catch(Exception e)
            {
                e.printStackTrace();
                //System.out.println((new StringBuilder("CoverURL = ")).append(s.getAlbumCoverUrl()).toString());
                albumCover = null;
            }
            ImageIcon ic;
            if(albumCover != null)
            {
                ic = new ImageIcon(albumCover);
                try
                {
                    BufferedImage bi = ImageIO.read(albumCover);
                    int width = bi.getWidth();
                    int ht = bi.getHeight();
                    int w = 150;
                    int h = 150;
                    ic = new ImageIcon(bi.getScaledInstance(w, h, 1));
                }
                catch(IOException e)
                {
                    e.printStackTrace();
                }
            } else
            {
                ic = new ImageIcon(getResource("phones.png"));
            }
			
			songInfo.setText(songDetails);
			img.setIcon(ic);
			img.repaint();
			songInfo.repaint();
        }

        private void updateUpComingSongLabel()
        {
			if (!usePDC) {
				return;
			}
				PandoraDesktopClient pdc = audioPlayer.pdc;
				Song s = audioPlayer.getNextSong();
				JLabel songLabel = pdc.getUpComingSongLabel();
			if(s == null)
            {
                songLabel.setVisible(false);
                return;
            } else
            {
                String song = s.getTitle();
                String artist = s.getArtist();
                String album = s.getAlbum();
                String nl = "<br>";
                String output = "<html>";
                output = (new StringBuilder(String.valueOf(output))).append("<u>Upcoming Song</u>").append(nl).toString();
                output = (new StringBuilder(String.valueOf(output))).append("Song : ").append(song).append(nl).toString();
                output = (new StringBuilder(String.valueOf(output))).append("Artist :  ").append(artist).append(nl).toString();
                output = (new StringBuilder(String.valueOf(output))).append("Album : ").append(album).append(nl).toString();
                output = (new StringBuilder(String.valueOf(output))).append("</html>").toString();
                songLabel.setText(output);
                songLabel.setVisible(true);
                return;
            }
        }

        public void run()
        {
            InputStream is;
            try
            {
				URL url = new URL(path);
				conn = (HttpURLConnection)url.openConnection();
				conn.setRequestMethod("GET");
				setRequestHeaders(conn);
				conn.connect();
                is = conn.getInputStream();
            }
            catch(IOException e)
            {
                audioPlayer.donePlayingCallBack();
                return;
            }
            try
            {
                BufferedInputStream bis = new BufferedInputStream(is, 0x19000);
                player = new Player(bis);
                try
                {
                    trackSong();
                    player.play();
					audioPlayer.player = player;
                    audioPlayer.pdc.resetRatingButtons();
                }
                catch(Exception e)
                {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(audioPlayer.pdc.getFrame(), "Playback of this song stopped due a networking error.", "It will now be skipped!", 2);
                }
                audioPlayer.donePlayingCallBack();
            }
            catch(Exception e)
            {
                e.printStackTrace();
            }
            return;
        }

        private String path;
        private HttpURLConnection conn;
        private Player player;
        private AudioPlayer audioPlayer;
        private Song s;
        private static final String USER_AGENT = "Mozilla/5.0 (compatible; MSIE 9.0; Windows NT 6.1; WOW64; Trident/5.0)";
        private static final String ACCEPT_ENCODING = "gzip, deflate";
        private static final String ACCEPT = "*/*";

        public SongPlayer(Song s, AudioPlayer audioPlayer)
        {
            path = s.getAudioUrl();
			currentSongURL = path;
			//get download url from path
            this.audioPlayer = audioPlayer;
            this.s = s;
        }
    }
	
	public Player player;
	public Player getPlayer() {
		if (player != null) {
			return player;
		} else {
			return null;
		}
	}
	
	public String currentSongURL;


    public AudioPlayer(Station s, JLabel albumArt, JLabel songInfo, PandoraDesktopClient pdc)
    {
        currentIndex = 0;
        SONG_LIST_SIZE_ZERO_BASED = 3;
        this.s = s;
        this.albumArt = albumArt;
        this.songInfo = songInfo;
        isPlaying = false;
        this.pdc = pdc;
        playedSongs = new HashMap(20);
    }
	
	public AudioPlayer(Station s) {
		usePDC = false;
        currentIndex = 0;
        SONG_LIST_SIZE_ZERO_BASED = 3;
        this.s = s;
        isPlaying = false;
        playedSongs = new HashMap(20);
	}

    public void replayCurrentSong()
    {
        if(sp != null && sp.isAlive())
        {
            sp.stopPlayBack();
            play();
        }
    }
	
	public void saveCurrentSongToDisk() {
		Song s = getCurrentSong();
		String destDir = (new StringBuilder(String.valueOf(System.getProperty("user.home")))).append(File.separator).append("Documents").append(File.separator).append("PDC").append(File.separator).append(s.getArtist()).append(File.separator).toString();
		final String destFile = (new StringBuilder(String.valueOf(destDir))).append(s.getTitle()).append("_").append(s.getArtist()).append(".mp3").toString();
        (new Thread() {
			public void run() {
				File file = new File(destDir + destFile);
				if (file.exists()) {
					songExists(s);
					return;
				}
				saveSong();
			}
			
			public void saveSong() {
				try {
					File mainDir = new File(destDir.replace(s.getArtist() + File.separator, ""));
					File destDirFile = new File(destDir);
					//If folders dont exist, create them
					if (!mainDir.exists()) {
						mainDir.mkdir();
					}
					if (!destDirFile.exists()) {
						destDirFile.mkdir();
					}
					setDownloadLabel();
					//Read the song from url
                    URL url = new URL(s.getAudioUrl());
                    HttpURLConnection conn = (HttpURLConnection)url.openConnection();
                    conn.setRequestProperty("User-Agent", "Mozilla/5.0 (compatible; MSIE 9.0; Windows NT 6.1; WOW64; Trident/5.0)");
                    conn.setRequestProperty("Accept-Encoding", "gzip, deflate");
                    conn.setRequestProperty("Accept", "*/*");
                    conn.connect();
					//System.out.println("Song Size: " + conn.getContentLength());
					long minSizeMB = 1;
					long expectedSizeInBytes = 1024 * 1024 * minSizeMB;
					if (conn.getContentLength() < expectedSizeInBytes) {
						System.out.println("Could not download song...");
						onDownloadCompletion(true, s);
						return;
					}
					BufferedInputStream songIn = new BufferedInputStream(conn.getInputStream());
					String destFileFixed = destFile.replace(" / ", "-");
					destFileFixed = destFileFixed.replace("\\", "/");
					File file = new File(destFileFixed);
					FileOutputStream songOut = new FileOutputStream(file);
					byte[] dataBuffer = new byte[1024];
					int bytesRead;
					while((bytesRead = songIn.read(dataBuffer, 0, 1024)) != -1) {
						songOut.write(dataBuffer, 0, bytesRead);
					}
					songOut.close();
					saveMetaData(destFileFixed, s);
					onDownloadCompletion(false, s);
					System.out.println("Saved Song: " + s.getTitle());
					pdc.enableSave(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			
			int retryAttempt = 0;
			int maxAttempts = 3;
            private void saveMetaData(String filename, Song s) {
                String artist = s.getArtist();
                String album = s.getAlbum();
                String song = s.getTitle();
				File f = new File(filename);
                try {
                    AudioFile af = AudioFileIO.read(f);
                    Tag tag = af.getTagOrCreateAndSetDefault();
                    tag.setField(FieldKey.ARTIST, artist);
                    tag.setField(FieldKey.TITLE, song);
                    tag.setField(FieldKey.ALBUM, album);
                    af.commit();
                } catch(Exception exception) {
					if (retryAttempt < maxAttempts) {
						f.delete();
						retryAttempt++;
						saveSong();
					} else {
						f.delete();
						retryAttempt = 0;
						onDownloadCompletion(true, s);
					}
				}
            }
		}).start();
	}
	
	public int failureCount = 0;
	public long timeLastSong = 0;
	
	public void songExists(Song s) {
		JLabel lbl = pdc.getCurrentUser();
		String title = getCurrentSong().getTitle();
		String artist = getCurrentSong().getArtist();
        String userText = String.format("Connected As : %s", new Object[] {
            pdc.getUser(), pdc.getUser()
        });
		String text = "";
		long currentTimeMillis = System.currentTimeMillis();
		long timeSinceLast = timeLastSong - currentTimeMillis;
		timeLastSong = currentTimeMillis;
		String songInfo = String.format("%s by  %s already saved!", new Object[] {
			title, artist
		});
		text = String.format("<html> %s <br/><strong><font color=\"Red\"> %s </font></strong></html>", new Object[] {
			userText, songInfo
		});
        lbl.setText(text);
		(new Thread() {
			public void run() {
				try {
					Thread.sleep(5000);
					resetDownloadLabel();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}).start();
	}
	
	public void onDownloadCompletion(boolean failure, Song s) {
		JLabel lbl = pdc.getCurrentUser();
		String title = getCurrentSong().getTitle();
		String artist = getCurrentSong().getArtist();
        String userText = String.format("Connected As : %s", new Object[] {
            pdc.getUser(), pdc.getUser()
        });
		String text = "";
		long currentTimeMillis = System.currentTimeMillis();
		long timeSinceLast = timeLastSong - currentTimeMillis;
		timeLastSong = currentTimeMillis;
		if (failure) {
			if (timeSinceLast <= 500) {
				if (failureCount < 3) {
					failureCount++;
				} else {
					failureCount = 0;
					pdc.doRelogin();
				}
			}
			String songInfo = String.format("Failed to download %s by  %s...", new Object[] {
				title, artist
			});
			text = String.format("<html> %s <br/><strong><font color=\"Red\"> %s </font></strong></html>", new Object[] {
				userText, songInfo
			});
		} else {
			String songInfo = String.format("Downloaded %s by  %s...", new Object[] {
				title, artist
			});
			text = String.format("<html> %s <br/><strong><font color=\"Green\"> %s </font></strong></html>", new Object[] {
				userText, songInfo
			});
		}
        lbl.setText(text);
		(new Thread() {
			public void run() {
				try {
					Thread.sleep(5000);
					resetDownloadLabel();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}).start();
	}

    private void setDownloadLabel()
    {
        JLabel lbl = pdc.getCurrentUser();
        String title = getCurrentSong().getTitle();
        String artist = getCurrentSong().getArtist();
        String userText = String.format("Connected As : %s", new Object[] {
            pdc.getUser(), pdc.getUser()
        });
        String songInfo = String.format("Downloading %s by  %s...", new Object[] {
            title, artist
        });
        String text = String.format("<html> %s <br/><strong><font color=\"Green\"> %s </font></strong></html>", new Object[] {
            userText, songInfo
        });
        lbl.setText(text);
    }

    private void resetDownloadLabel()
    {
        String user = pdc.getUser().getUser();
        JLabel lbl = pdc.getCurrentUser();
        String text = String.format("Connected As : %s", new Object[] {
            user
        });
        lbl.setText(text);
    }

    /*public void onDownloadCompletion(boolean failure, Song s)
    {
        if(!failure)
            pdc.showDownloadCompletionDialog(0, s);
        else
            pdc.showDownloadCompletionDialog(1, s);
        pdc.getSaveMenuItem().setEnabled(true);
        resetDownloadLabel();
    }*/

    public Station getStation()
    {
        return s;
    }

    public PandoraDesktopClient getPDC()
    {
        return pdc;
    }

    public Song getCurrentSong()
    {
        if(sp != null)
            return playlist[currentIndex];
        else
            return null;
    }

    public Song getNextSong()
    {
        int nextSongIndex = currentIndex + 1;
        if(nextSongIndex <= SONG_LIST_SIZE_ZERO_BASED)
            return playlist[nextSongIndex];
        else
            return null;
    }

    public JLabel getAlbumArt()
    {
        return albumArt;
    }

    public JLabel getSongInfoLabel()
    {
        return songInfo;
    }

    public String getCurrentStation()
    {
        return s.getName();
    }

    private void getNewSongs()
    {
		if (usePDC) {
			//System.out.println((new StringBuilder("In this call to getNewSongs, radio is Alive?")).append(pdc.getRadio().isAlive()).append(". Radio-Ref=").append(pdc.getRadio()).toString());
        }
		if(currentIndex > SONG_LIST_SIZE_ZERO_BASED || playlist == null || playlist.length == 0)
        {
            //System.out.print(". We are getting new songs..");
            try
            {
                playlist = s.getPlaylist("mp3-hifi");
                SONG_LIST_SIZE_ZERO_BASED = playlist.length - 1;
            }
            catch(Exception e)
            {
                //System.out.println("2 - Error occured. About to reload the radio station again. ");
                //System.out.println((new StringBuilder("err:\n")).append(e).toString());
                
				if (usePDC) {
					if(!pdc.isRadioAlive())
                    reloadStation();
				}
            }
            currentIndex = 0;
        }
    }

    public void setStation(Station other)
    {
        if(s.equals(other))
        {
            return;
        } else
        {
            s = other;
            currentIndex = 0;
            playlist = null;
            return;
        }
    }

    public void reloadStation()
    {
        PandoraRadio radio = pdc.getRadio();
        Long currentStationId = Long.valueOf(this.s.getId());
        //System.out.println((new StringBuilder("[inMethod] of  Reloading Stations ... radioNow = ")).append(radio).toString());
        radio.disconnect();
        PandoraUser user = pdc.getUser();
        PandoraRadio newRadio = new JSONPandoraRadio();
        newRadio.connect(user.getUser(), user.getPass());
        radio = pdc.setRadio(newRadio);
        //System.out.println((new StringBuilder("[inMethod] After reloading... radioNow = ")).append(radio).toString());
        //System.out.println((new StringBuilder("[inMethod] After reloading... newPandoraRadio Actual = ")).append(newRadio).toString());
        try
        {
            ArrayList stations = radio.getStations();
            pdc.setStations(stations);
            Station s = radio.getStationById(currentStationId.longValue());
            setStation(s);
            playlist = s.getPlaylist();
            SONG_LIST_SIZE_ZERO_BASED = playlist.length - 1;
        }
        catch(Exception e)
        {
            //System.out.println("An error occurred while attempting to reload stations. Stations have NOT been reloaded succesfully");
            e.printStackTrace();
        }
    }

    public void stop()
    {
        if(isPlaying)
            sp.stopPlayBack();
        isPlaying = false;
    }

    public void play()
    {
        if(isPlaying && sp != null && sp.isAlive())
        {
            sp.stopPlayBack();
            sp = null;
        }
		if (usePDC) {
			if(!pdc.isRadioAlive())
				reloadStation();
        }
		getNewSongs();
        Song s = playlist[currentIndex];
        sp = new SongPlayer(s, this);
        sp.play();
        isPlaying = true;
    }

    public void skipSong()
    {
        if(sp != null && sp.isAlive())
            sp.skipSong();
    }

    public void setVolume(float vol)
    {
        if(vol >= 0.0F && (double)vol <= 1.0D)
        {
            return;
        } else
        {
            adjustVolume(vol);
            return;
        }
    }

    private void adjustVolume(float vol)
    {
        javax.sound.sampled.Mixer.Info infos[] = AudioSystem.getMixerInfo();
        javax.sound.sampled.Mixer.Info ainfo[];
        int j = (ainfo = infos).length;
        for(int i = 0; i < j; i++)
        {
            javax.sound.sampled.Mixer.Info info = ainfo[i];
            Mixer mixer = AudioSystem.getMixer(info);
            if(mixer.isLineSupported(javax.sound.sampled.Port.Info.SPEAKER))
            {
                Port port;
                try
                {
                    port = (Port)mixer.getLine(javax.sound.sampled.Port.Info.SPEAKER);
                    port.open();
                }
                catch(LineUnavailableException e)
                {
                    e.printStackTrace();
                    return;
                }
                if(port.isControlSupported(javax.sound.sampled.FloatControl.Type.VOLUME))
                {
                    FloatControl volume = (FloatControl)port.getControl(javax.sound.sampled.FloatControl.Type.VOLUME);
                    volume.setValue(vol);
                    //System.out.println((new StringBuilder("After Vol: ")).append(volume).toString());
                }
                port.close();
            }
        }

    }

    public void donePlayingCallBack()
    {
        //System.out.println((new StringBuilder("WE RECEIVED THE DONE PLAYING CALLBACK NOW. still Playing?")).append(isPlaying).toString());
        if(isPlaying)
        {
            currentIndex++;
            sp = null;
            play();
        } else
        {
            sp = null;
        }
    }

    private Station s;
    private Song playlist[];
    public int currentIndex;
    public boolean isPlaying;
    private int SONG_LIST_SIZE_ZERO_BASED;
    private HashMap playedSongs;
    private JLabel albumArt;
    private JLabel songInfo;
    private PandoraDesktopClient pdc;
    private SongPlayer sp;
	public boolean usePDC = true;
	public boolean autoDownload;

}
