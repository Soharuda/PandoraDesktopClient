import com.jgoodies.forms.factories.DefaultComponentFactory;
import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.*;
import java.awt.*;
import java.awt.event.*;
import java.io.PrintStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Iterator;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.io.File;

public class Login extends JFrame
{

    public static void main(String args[])
    {
        EventQueue.invokeLater(new Runnable() {

            public void run()
            {
                try
                {
                    Login frame = new Login(null, null);
                    frame.setVisible(true);
                }
                catch(Exception e)
                {
                    e.printStackTrace();
                }
            }

        });
    }
	
	public void setFields(String user, String pass) {
		txtUsername.setText(user);
		passwordField.setText(pass);
	}
	
    public Login(PandoraRadio radio, PandoraDesktopClient pdc)
    {
        setIcon();
        currentUser = pdc.getCurrentUser();
        list = pdc.getList();
        panelStations = pdc.getPanelStations();
        this.pdc = pdc;
        setDefaultCloseOperation(3);
        setBounds(100, 100, 480, 250);
        contentPane = new JPanel();
        contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        contentPane.setLayout(new BorderLayout(0, 0));
        setContentPane(contentPane);
        JPanel panel = new JPanel();
        panel.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Connect to Pandora Radio Service", 4, 2, null, new Color(0, 0, 0)));
        contentPane.add(panel, "Center");
        panel.setLayout(new FormLayout(new ColumnSpec[] {
            ColumnSpec.decode("max(27dlu;default)"), FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("default:grow")
        }, new RowSpec[] {
            FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, 
            FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC
        }));
        JLabel lblUsername = DefaultComponentFactory.getInstance().createLabel("Username");
        lblUsername.setFont(new Font("Verdana", 1, 12));
        panel.add(lblUsername, "1, 4, right, default");
        txtUsername = new JTextField();
        panel.add(txtUsername, "3, 4, fill, default");
        txtUsername.setColumns(10);
        JLabel lblPassword = DefaultComponentFactory.getInstance().createLabel("Password");
        lblPassword.setFont(new Font("Verdana", 1, 12));
        panel.add(lblPassword, "1, 8, right, default");
        passwordField = new JPasswordField();
        panel.add(passwordField, "3, 8, fill, default");
        lblLoginmsg = new JLabel("");
        panel.add(lblLoginmsg, "1, 10, 3, 1");
        JButton btnLogin = new JButton("Login");
        panel.add(btnLogin, "1, 12, 3, 1");
        btnLogin.addMouseListener(new MouseAdapter() {

            public void mouseClicked(MouseEvent mouseevent)
            {
				doLogin();
            }
        });
		txtUsername.addKeyListener(fieldKeyListener());
		passwordField.addKeyListener(fieldKeyListener());
    }
	
	private KeyListener fieldKeyListener() {
		return new KeyListener() {
			public void keyPressed(KeyEvent keyEvent) {
				switch(keyEvent.getKeyCode()) {
					case 10:
						doLogin();
						break;
				}
			}
			public void keyReleased(KeyEvent keyEvent) {
			}
			public void keyTyped(KeyEvent keyEvent) {
			}
		};
	}
	
	public void autoLogin(String autoUser, String autoPass) {
		txtUsername.setText(autoUser);
		passwordField.setText(autoPass);
		doLogin();
	}

    public void doLogin() {
        String user = txtUsername.getText();
        String pass = new String(passwordField.getPassword());
        boolean incompatProtocol = false;
        try {
            radio = new JSONPandoraRadio();
            radio.sync();
            radio.connect(user, pass);
            pdc.setRadio(radio);
        } catch(Throwable exp) {
            System.out.println("sdfsdfsdf - error occured");
            if(exp.getMessage().toLowerCase().contains("incompatible_protocol"))
                incompatProtocol = true;
        }
        if(radio.isAlive()) {
            onSuccessLogin();
            pdc.setUser(new PandoraUser(user, pass));
        } else if(radio.incompat) {
            onIncompatProtocol();
        } else {
            onFailedLogin();
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

    private void setIcon()
    {
        URL url = getResource("minilogo.png");
        Toolkit kit = Toolkit.getDefaultToolkit();
        java.awt.Image img = kit.createImage(url);
        setIconImage(img);
    }

    private void onFailedLogin()
    {
        lblLoginmsg.setText("<html><font color=\"Red\"> Login Failed. Please make sure you have entered the correct credentials (including your full email address as the username) and try again.</font> </html>");
        PandoraDesktopClient.loggedIn = false;
    }

    private void onIncompatProtocol()
    {
        lblLoginmsg.setText("<html><font color=\"Blue\"> Login Attempt Failed as the Pandora Protocol has changed. Please check and update to a newer version of this application when it becomes available.</font> </html>");
        PandoraDesktopClient.loggedIn = false;
    }

    private void onSuccessLogin()
    {
        lblLoginmsg.setText("<html><font color=\"Green\"> Successfully Connected to Pandora</font> </html>");
        currentUser.setText((new StringBuilder("Connected As : ")).append(txtUsername.getText()).toString());
        PandoraDesktopClient.loggedIn = true;
		pdc.usern = txtUsername.getText();
		pdc.pass = passwordField.getText();
        setVisible(false);
        ArrayList stationList = null;
        try
        {
            stationList = radio.getStations();
        }
        catch(Throwable e)
        {
            System.out.println("Could not retrieve user stations...");
            e.printStackTrace();
        }
        String stationArray[] = new String[stationList.size()];
        int i = 0;
        for(Iterator iterator = stationList.iterator(); iterator.hasNext();)
        {
            Station s = (Station)iterator.next();
            stationArray[i++] = s.toString();
        }

        PandoraDesktopClient.list = list = new JList(stationArray);
        JScrollPane scroll = new JScrollPane(list);
        panelStations.add(scroll);
        pdc.setStations(stationList);
		pdc.setup(scroll);
        pdc.getFrame().setVisible(true);
        dispose();
    }

    private JPanel contentPane;
    public JPasswordField passwordField;
    public JTextField txtUsername;
    private JLabel lblLoginmsg;
    private JLabel currentUser;
    private JList list;
    private JSONPandoraRadio radio;
    private JPanel panelStations;
    private PandoraDesktopClient pdc;

}
