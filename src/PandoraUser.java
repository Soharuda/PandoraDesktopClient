// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   PandoraUser.java


public class PandoraUser
{

    public PandoraUser(String user, String pass)
    {
        this.user = user;
        this.pass = pass;
    }

    public String getPass()
    {
        return pass;
    }

    public String getUser()
    {
		if (auto) {
			return user + "    Autosaving";
		} else {
			return user;
		}
        //return user;
    }

    public String toString()
    {
        return user;
    }
	
	public void setAuto(boolean auto) {
		this.auto = auto;
	}

    private String user;
    private String pass;
	private boolean auto;
}
