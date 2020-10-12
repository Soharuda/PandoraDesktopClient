import java.awt.*;
import java.awt.event.*;
import java.io.PrintStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Iterator;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.border.TitledBorder;
import java.io.File;
import javazoom.jl.player.Player;
import java.util.HashMap;
import java.util.Map;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.Line.Info;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Port;

public class PandoraDesktopClient
{

    public static void main(String[] args)
    {
        try
        {
            System.setProperty("apple.laf.useScreenMenuBar", "true");
        }
        catch(Exception exception) { }
        EventQueue.invokeLater(new Runnable() {

            public void run()
            {
                try
                {
					PandoraDesktopClient window;
                    PandoraDesktopClient.useNimbus();
					/*Boolean autoLogin = true;
					String autoUser = "kenshin1388@hotmail.com";
					String autoPass = "Bonk5037";
					Boolean autoSaveSongs = true;*/
					Boolean autoLogin = false;
					String autoUser = "";
					String autoPass = "";
					Boolean autoSaveSongs = true;
					if (args.length > 0) {
						try {
							for (int i = 0; i < args.length; i++) {
								System.out.println(args[i]);
								if (args[i].trim().toLowerCase().equals("--login")) {
									autoUser = args[i + 1];
									autoPass = args[i + 2];
								}
								if (args[i].trim().toLowerCase().equals("--autosave")) {
									autoSaveSongs = true;
								}
							}
						} catch (Exception e) {
							e.printStackTrace();
						}
						window = new PandoraDesktopClient(autoLogin, autoUser, autoPass, autoSaveSongs);
					} else {
						window = new PandoraDesktopClient(autoLogin, autoUser, autoPass, autoSaveSongs);
					}
                    window.frame.setResizable(false);
                }
                catch(Exception e)
                {
                    e.printStackTrace();
                }
            }

        });
    }

    private static void useNimbus()
    {
        try
        {
			System.setProperty("sun.java2d.opengl", "true");
            javax.swing.UIManager.LookAndFeelInfo alookandfeelinfo[];
            int j = (alookandfeelinfo = UIManager.getInstalledLookAndFeels()).length;
            for(int i = 0; i < j; i++)
            {
                javax.swing.UIManager.LookAndFeelInfo info = alookandfeelinfo[i];
                if(!"Nimbus".equals(info.getName()))
                    continue;
                UIManager.setLookAndFeel(info.getClassName());
                break;
            }

        }
        catch(Exception exception) { }
    }
	
	public void doRelogin() {
		login.doLogin();
	}

    public JMenuItem getSaveMenuItem()
    {
        return mntmSave;
    }

    public JFrame getFrame()
    {
        return frame;
    }

    public void setStations(ArrayList s)
    {
        stations = s;
    }

    public PandoraUser getUser()
    {
        return user;
    }

    public void setUser(PandoraUser u)
    {
        user = u;
    }

    public PandoraRadio getRadio()
    {
        return radio;
    }

    public PandoraRadio setRadio(PandoraRadio pr)//, JSONPandoraRadio json)
    {
        radio = pr;
		pandoraRadio = (JSONPandoraRadio) pr;
        return radio;
    }

    public JList getList()
    {
        return list;
    }

    public JLabel getCurrentUser()
    {
        return currentUser;
    }

    public JPanel getPanelStations()
    {
        return panelStations;
    }

    public JLabel getUpComingSongLabel()
    {
        return lblUpcomingSong;
    }

    private void setImageURL()
    {
    }

    public void showThumbsUP()
    {
        btnVoteUp.setState(2);
        btnVoteDown.setState(0);
    }

    private String[] getStationArray()
    {
        int index = 0;
        int size = stations.size();
        String array[] = new String[size];
        for(Iterator iterator = stations.iterator(); iterator.hasNext();)
        {
            Station s = (Station)iterator.next();
            array[index++] = s.toString();
        }

        return array;
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

    private void setIcon()
    {
        URL url = getResource("minilogo.png");
        Toolkit kit = Toolkit.getDefaultToolkit();
        java.awt.Image img = kit.createImage(url);
        getFrame().setIconImage(img);
    }

    public void setStationsList(ArrayList list)
    {
        stations = list;
    }

    public boolean isRadioAlive()
    {
        return radio.isAlive();
    }

    public PandoraDesktopClient(Boolean autoLogin, String autoUser, String autoPass, Boolean autoSaveSongs)
    {
		this.autoDownload = autoSaveSongs;
        stationSelected = -1;
        setImageURL();
        initialize(autoLogin, autoUser, autoPass);
    }

    private void initialize(Boolean autoLogin, String autoUser, String autoPass)
    {
        frame = new JFrame("Pandora Desktop Client");
        frame.setBounds(100, 100, 725, 425);
		frame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				doClose();
			}
		});
        JMenuBar menuBar = new JMenuBar();
        frame.setJMenuBar(menuBar);
        JMenu mnNewMenu = new JMenu("Pandora");
        menuBar.add(mnNewMenu);
        mntmSave = new JMenuItem("Save Song");
        mnNewMenu.add(mntmSave);
		autoSave = new JMenuItem("AutoSave");
		mnNewMenu.add(autoSave);
        JMenuItem mntmViewHistory = new JMenuItem("View Playback History");
        JMenuItem mntmReload = new JMenuItem("Reload Pandora");
		JMenuItem mntmFixUI = new JMenuItem("Reload UI");
		mnNewMenu.add(mntmFixUI);
        mnNewMenu.add(mntmReload);
        JMenuItem mntmConnect = new JMenuItem("Connect");
        JMenuItem mntmDisconnect = new JMenuItem("Disconnect");
        JMenuItem mntmExit = new JMenuItem("Exit");
        mnNewMenu.add(mntmExit);
        JMenu mnHelp = new JMenu("Help");
        menuBar.add(mnHelp);
        JMenuItem mntmAbout = new JMenuItem("About");
        mnHelp.add(mntmAbout);
        JPanel panelBottom = new JPanel();
        frame.getContentPane().add(panelBottom, "South");
        panelBottom.setLayout(new FlowLayout(1, 5, 5));
        JButton btnPlay = new JButton("Play");
        btnPlay.setMaximumSize(new Dimension(57, 28));
        btnPlay.setMinimumSize(new Dimension(57, 28));
        btnPlay.setPreferredSize(new Dimension(57, 28));
        panelBottom.add(btnPlay);
		JButton btnTest = new JButton("Test");
        btnTest.setMaximumSize(new Dimension(57, 28));
        btnTest.setMinimumSize(new Dimension(57, 28));
        btnTest.setPreferredSize(new Dimension(57, 28));
        panelBottom.add(btnTest);
        JButton btnStop = new JButton("Stop");
        panelBottom.add(btnStop);
        JButton btnNextSong = new JButton("Skip Song");
        btnVoteUp = new ThumbUp();
        btnVoteDown = new ThumbDown();
        JButton btnReplay = new JButton("Replay Song");
        volume = new VolumeControl();
		//volumeSlider = new JSlider(0, 100, 0);
		//volumeSlider.setValue(100);
		//volumeSlider.addChangeListener(new ChangeListener() {
		//	public void stateChanged(ChangeEvent ce) {
		//		doVolume();
		//	}
		//});
        panelBottom.add(btnReplay);
        panelBottom.add(btnNextSong);
        panelBottom.add(btnVoteDown);
        panelBottom.add(btnVoteUp);
        JLabel lblFiller = new JLabel();
        lblFiller.setPreferredSize(new Dimension(10, 5));
        panelBottom.add(lblFiller);
        panelBottom.add(volume);
        //panelBottom.add(volumeSlider);
		currentUser = new JLabel();
        JPanel panelTop = new JPanel();
        panelTop.add(currentUser);
        frame.getContentPane().add(panelTop, "North");
        JPanel panelCenter = new JPanel();
        panelCenter.setBorder(new TitledBorder(null, "Current Song", 4, 2, null, null));
        frame.getContentPane().add(panelCenter, "Center");
        panelCenter.setLayout(new BorderLayout(0, 0));
        lblAlbumArt = new JLabel(new ImageIcon(getResource("phones.png")));
        panelCenter.add(lblAlbumArt, "Center");
        panelSongInfo = new JPanel(new GridLayout(1, 2));
        lblSonginfo = new JLabel();
        lblUpcomingSong = new JLabel();
        panelSongInfo.add(lblSonginfo);
        panelSongInfo.add(lblUpcomingSong);
        panelCenter.add(panelSongInfo, "South");
        panelStations = new JPanel();
        panelStations.setBorder(new TitledBorder(null, "Select Station", 4, 2, null, null));
        frame.getContentPane().add(panelStations, "West");
        panelStations.setLayout(new GridLayout(1, 1, 0, 0));
        attachListeners(btnTest, btnPlay, btnStop, btnNextSong, btnVoteDown, btnVoteUp, btnReplay, lblAlbumArt, lblSonginfo, list);
        attachMenuListeners(autoSave, mntmConnect, mntmDisconnect, mntmAbout, mntmExit, mntmSave, mntmReload, mntmFixUI, panelStations);
        showLoginFirst(autoLogin, autoUser, autoPass);
        setIcon();
		config = new Config(this);
		enableSave(false);
    }
	
	//public void createFromArtist() {
	//	Map data = new HashMap();
	//	data.put("searchText", "encore");
	//	data.put("userAuthToken", pandoraRadio.userAuthToken);
	//	data.put("syncTime", "1335869287");
	//	String stringData = (new Gson()).toJson(data);
	//	pandoraRadio.doPost(URL, INPUT);
	//}
	
	public void doVolume() {
		try {
			float newVolume = (float) volumeSlider.getValue() / 100;
			Info source = Port.Info.SPEAKER;
			Port outline = (Port) AudioSystem.getLine(source);
			FloatControl volumeControl = (FloatControl) outline.getControl(FloatControl.Type.VOLUME);
			volumeControl.setValue(newVolume);
			//System.out.println("Volume: " + newVolume);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void setup(JScrollPane stationScrollPane) {
		list.addKeyListener(stationKeyListener());
		list.addMouseListener(stationMouseListener());
	}
	
	private MouseListener stationMouseListener() {
		return new MouseListener() {
			public void mouseExited(MouseEvent mouseEvent) {
			}
			public void mouseEntered(MouseEvent mouseEvent) {
			}
			public void mouseReleased(MouseEvent mouseEvent) {
			}
			public void mousePressed(MouseEvent mouseEvent) {
			}
			public void mouseClicked(MouseEvent mouseEvent) {
				if (mouseEvent.getClickCount() == 2) {
					doStationPlay();
				}
			}
		};
	}
	
	private KeyListener stationKeyListener() {
		return new KeyListener() {
			public void keyPressed(KeyEvent keyEvent) {
				if(keyEvent.getKeyCode() == 10) {
					doStationPlay();
				}
			}
			public void keyReleased(KeyEvent keyEvent) {
			}
			public void keyTyped(KeyEvent keyEvent) {
			}
		};
	}
	
	public void doStationPlay() {
		int selection = list.getSelectedIndex();
		if(selection == -1)
			return;
		if(selection == stationSelected && ap.isPlaying)
			return;
		if(!radio.isAlive() && ap != null)
		{
			ap.reloadStation();
		}
		Station curr = (Station)stations.get(selection);
		if(ap != null)
		{
			if(ap.isPlaying)
				ap.stop();
			ap.setStation(curr);
			stationSelected = selection;
			ap.play();
		} else
		{
			ap = new AudioPlayer(curr, lblAlbumArt, lblSonginfo, PandoraDesktopClient.this);
			ap.autoDownload = autoDownload;
			ap.play();
			stationSelected = selection;
			enableSave(true);
		}
	}

    public JPanel getSongInfoPanel()
    {
        return panelSongInfo;
    }
	
	public void doClose() {
			config.saveConfig(user, autoDownload);
            radio.disconnect();
            System.exit(0);
	}

    public void resetRatingButtons()
    {
        btnVoteDown.resetToDefault();
        btnVoteUp.resetToDefault();
    }

    private void attachListeners(JButton test, JButton play, JButton stop, JButton skip, JLabel voteDown, JLabel voteUp, JButton replay, JLabel albumArt, 
            JLabel songInfo, JList list)
    {
        play.addActionListener(getPlayListener(list, albumArt, songInfo));
		test.addActionListener(getTestListener());
        stop.addActionListener(getStopListener());
        skip.addActionListener(getSkipSongListener());
        voteDown.addMouseListener(getVoteDownListener());
        voteUp.addMouseListener(getVoteUpListneer());
        replay.addActionListener(getReplayListener());
    }

    private void attachMenuListeners(JMenuItem autoSave, JMenuItem connect, JMenuItem disconnect, JMenuItem about, JMenuItem exit, JMenuItem save, JMenuItem reload, JMenuItem fixUI, JPanel stations)
    {
		autoSave.addActionListener(autoSaveListener());
        disconnect.addActionListener(getDisconnectMenuListener());
        about.addActionListener(getAboutMenuListener());
        exit.addActionListener(getExitMenuListener());
        save.addActionListener(getSaveMenuListener());
        reload.addActionListener(getReloadListener());
		fixUI.addActionListener(getFixUIListener());
    }

    private void showLoginFirst(Boolean autoLogin, String autoUser, String autoPass)
    {
        frame.setVisible(false);
        getConnectMenuListener().actionPerformed(null);
        frame.setVisible(false);
		login = new Login(radio, PandoraDesktopClient.this);
        login.setVisible(true);
		if (autoLogin) {
			login.autoLogin(autoUser, autoPass);
		}
    }

    public void showDownloadCompletionDialog(int code, Song s)
    {
		String song = "";
		String artist = "";
		if (code != 2) {
			song = s.getTitle();
			artist = s.getArtist();
		}
        switch(code)
        {
        case 0: // '\0'
            JOptionPane.showMessageDialog(frame, (new StringBuilder("Download of ")).append(song).append(" by ").append(artist).append(" is completed.").append("\n").append("You can view the file in your Documents Folder.").append("\n").toString(), "Successful Download", 1);
            break;

        case 1: // '\001'
            JOptionPane.showMessageDialog(frame, (new StringBuilder("Sorry, Download of ")).append(song).append(" by ").append(artist).append(" has failed.").append("\n").append("If the song is still playing, you can try downloading again.").append("\n").toString(), "Download Failed", 0);
            break;
		case 2:
			if (autoDownload) {
				JOptionPane.showMessageDialog(frame, "Enabled", "Auto Download", 1);
			} else {
				JOptionPane.showMessageDialog(frame, "Disabled", "Auto Download", 0);
			}
			break;
        }
    }

    private ActionListener getReloadListener()
    {
        return new ActionListener() {

            public void actionPerformed(ActionEvent e)
            {
                AudioPlayer audioPlayer = ap;
                if(audioPlayer.isPlaying)
                    audioPlayer.stop();
                audioPlayer.reloadStation();
            }
        };
    }

    private ActionListener getViewHistoryListener()
    {
        return new ActionListener() {

            public void actionPerformed(ActionEvent actionevent)
            {
            }
        };
    }
	
	private ActionListener getFixUIListener() {
		return new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				//frame.repaint();
				frame.revalidate();
				//frame.doLayout();
			}
		};
	}

    private ActionListener getSaveMenuListener()
    {
        return new ActionListener() {

            public void actionPerformed(ActionEvent e)
            {
                ap.saveCurrentSongToDisk();
				enableSave(false);
            }
        };
    }
	
	public void enableSave(boolean value) {
		mntmSave.setEnabled(value);
	}

    private ActionListener autoSaveListener()
    {
        return new ActionListener() {

            public void actionPerformed(ActionEvent e)
            {
                autoDownload = !autoDownload;
				if (ap != null) {
					ap.autoDownload = autoDownload;
				}
				showDownloadCompletionDialog(2, null);
            }
        };
    }

    private ActionListener getDisconnectMenuListener()
    {
        JFrame frm = frame;
        return new ActionListener() {

            public void actionPerformed(ActionEvent actionevent)
            {
            }
        };
    }

    private ActionListener getExitMenuListener()
    {
        return new ActionListener() {

            public void actionPerformed(ActionEvent e)
            {
				doClose();
            }
        };
    }

    private ActionListener getAboutMenuListener()
    {
        final JFrame frm = frame;
        return new ActionListener() {

            public void actionPerformed(ActionEvent e)
            {
                String copyright = "\251";
                JOptionPane.showMessageDialog(frm, (new StringBuilder("Pandora Desktop Client Version 1.0\nA Jervis Muindi production\nAll rights reserved ")).append(copyright).append(" 2012").append("\n\n Repaied/Touched Up by: Soharuda\n 2020").toString(), "About", 1);
            }
        };
    }

    private ActionListener getConnectMenuListener()
    {
        return new ActionListener() {

            public void actionPerformed(ActionEvent e)
            {
                //login = new Login(radio, PandoraDesktopClient.this);
                //login.setVisible(true);
            }
        };
    }

    private ActionListener getReplayListener()
    {
        return new ActionListener() {

            public void actionPerformed(ActionEvent e)
            {
                if(ap == null)
                    return;
                if(ap.isPlaying)
                    ap.replayCurrentSong();
            }
        };
    }
	
    private ActionListener getTestListener()
    {
        return new ActionListener() {

            public void actionPerformed(ActionEvent e)
            {
				pandoraRadio.createStationTest();
			}
		};
	}

    private ActionListener getPlayListener(JList list, final JLabel albumArt, final JLabel songInfo)
    {
        return new ActionListener() {

            public void actionPerformed(ActionEvent e)
            {
                int selection = PandoraDesktopClient.list.getSelectedIndex();
                if(selection == -1)
                    return;
                if(selection == stationSelected && ap.isPlaying)
                    return;
                if(!radio.isAlive() && ap != null)
                {
                    ap.reloadStation();
                }
                Station curr = (Station)stations.get(selection);
                if(ap != null)
                {
                    if(ap.isPlaying)
                        ap.stop();
                    ap.setStation(curr);
                    stationSelected = selection;
                    ap.play();
                } else
                {
                    ap = new AudioPlayer(curr, albumArt, songInfo, PandoraDesktopClient.this);
					ap.autoDownload = autoDownload;
                    ap.play();
                    stationSelected = selection;
					enableSave(true);
                }
            }
        };
    }

    private MouseListener getVoteUpListneer()
    {
        return new MouseAdapter() {

            public void mouseClicked(MouseEvent e)
            {
                if(ap != null && ap.isPlaying)
                    try
                    {
                        radio.rate(ap.getStation(), ap.getCurrentSong(), true);
                        btnVoteUp.setState(2);
                        btnVoteDown.setState(0);
                    }
                    catch(Exception e1)
                    {
                        e1.printStackTrace();
                    }
            }
        };
    }

    private MouseListener getVoteDownListener()
    {
        return new MouseAdapter() {

            public void mouseClicked(MouseEvent e)
            {
                if(ap != null && ap.isPlaying)
                    try
                    {
                        radio.rate(ap.getStation(), ap.getCurrentSong(), false);
                        btnVoteDown.setState(2);
                        btnVoteUp.setState(0);
                        ap.skipSong();
                    }
                    catch(Exception e1)
                    {
                        e1.printStackTrace();
                    }
            }
        };
    }

    private ActionListener getSkipSongListener()
    {
        return new ActionListener() {

            public void actionPerformed(ActionEvent e)
            {
                ap.skipSong();
            }
        };
    }

    private ActionListener getStopListener()
    {
        return new ActionListener() {

            public void actionPerformed(ActionEvent e)
            {
                if(ap.isPlaying && ap != null)
                    ap.stop();
            }
        };
    }

    private JFrame frame;
    private PandoraRadio radio;
    private ArrayList stations;
    private int stationSelected;
    private Song playlist[];
    private AudioPlayer ap;
    private URL imageURL;
    public static boolean loggedIn = false;
    public static JList list;
    public Login login;
    private JLabel currentUser;
    private JPanel panelStations;
    private PandoraUser user;
    private JLabel lblUpcomingSong;
    private JLabel lblAlbumArt;
    private JLabel lblSonginfo;
    private JMenuItem mntmSave;
	private JMenuItem autoSave;
    private JPanel panelSongInfo;
    public static final int DOWNLOAD_SUCCESS = 0;
    public static final int DOWNLOAD_ERROR = 1;
    private static final String VERSION = "1.0";
    private VolumeControl volume;
    private ThumbDown btnVoteDown;
    private ThumbUp btnVoteUp;
	public boolean autoDownload;
	public Config config;
	public String usern = "";
	public String pass = "";
	private JSONPandoraRadio pandoraRadio;
	private JSlider volumeSlider;
}
