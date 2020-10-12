import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.DataInputStream;
import java.util.Random;
import java.util.Properties;

public class Config {
	
	public Config(PandoraDesktopClient pdc) {
		this.pdc = pdc;
		loadConfig();
	}
	
	public void loadConfig() {
		/*try {
			File configFile = new File(destDir + configFileName);
			DataInputStream dataIn = new DataInputStream(new FileInputStream(configFile));
			int userByteLength = dataIn.readInt();
			byte[] userBytes = new byte[userByteLength];
			for (int i = 0; i < userByteLength; i++) {
				userBytes[i] = dataIn.readByte();
			}
			String user = byteToString(CryptoUtils.decryptString(key, userBytes));
			int passByteLength = dataIn.readInt();
			byte[] passBytes = new byte[passByteLength];
			for (int i = 0; i < userByteLength; i++) {
				passBytes[i] = dataIn.readByte();
			}
			String pass = byteToString(CryptoUtils.decryptString(key, passBytes));
			boolean auto = Boolean.parseBoolean(dataIn.readUTF());
			pdc.login.setFields(user, pass);
			pdc.autoDownload = auto;
		} catch (Exception e) {
			e.printStackTrace();
		}*/
	}
	
	public String byteToString(byte[] data) {
		try {
			StringBuffer stringBuffer = new StringBuffer();
			for(int i = 0; i < data.length; i++) {
				stringBuffer.append((char)data[i]);
			}
			return stringBuffer.toString();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public void saveConfig(PandoraUser user, boolean auto) {
		/*try {
			File configFile = new File(destDir + configFileName);
			DataOutputStream dataOut = new DataOutputStream(new FileOutputStream(configFile));
			byte[] userBytes = CryptoUtils.encryptString(key, user.getUser());
			dataOut.writeInt(userBytes.length);
			for (byte m: userBytes) {
				dataOut.writeByte(m);
			}
			byte[] passBytes = CryptoUtils.encryptString(key, user.getPass());
			System.out.println(passBytes.length + "");
			dataOut.writeInt(passBytes.length);
			for (byte m: passBytes) {
				dataOut.writeByte(m);
			}
			if (auto) {
				dataOut.writeUTF("True");
			} else if (!auto) {
				dataOut.writeUTF("False");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}*/
	}
	
	public void genKey() {
		int leftLimit = 97;
		int rightLimit = 122;
		int lengthReq = 16;
		Random random = new Random();
		StringBuffer buffer = new StringBuffer();
		for (int i = 0; i < lengthReq; i++) {
			int randomChar = leftLimit + (int) (random.nextFloat() * (rightLimit - leftLimit + 1));
			buffer.append((char) randomChar);
		}
		System.out.println(buffer.toString());
	}
	
	//CryptoUtils.encryptString(key, s)
	//CryptoUtils.decryptString(key, stringData)
	private Properties config;
	private PandoraDesktopClient pdc;
	private String key = "ogeatnvyaqlxzvau";
	private String destDir = (new StringBuilder(String.valueOf(System.getProperty("user.home")))).append(File.separator).append("Documents").append(File.separator).append("PDC").append(File.separator).toString();
	private String configFileName = "PDCConfig.dat";
}