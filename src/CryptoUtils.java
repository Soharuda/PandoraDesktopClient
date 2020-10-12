import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.io.UnsupportedEncodingException;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.Charset;
 
/**
 * A utility class that encrypts or decrypts a file.
 * @author www.codejava.net
 *
 */
public class CryptoUtils {
    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES";
 
    public static void encryptFile(String key, File inputFile, File outputFile)
            throws CryptoException {
        doCryptoFile(Cipher.ENCRYPT_MODE, key, inputFile, outputFile);
    }
 
    public static void decryptFile(String key, File inputFile, File outputFile)
            throws CryptoException {
        doCryptoFile(Cipher.DECRYPT_MODE, key, inputFile, outputFile);
    }
	
	public static byte[] decryptString(String key, byte[] bytes)
			throws CryptoException {
		return doCryptoString(Cipher.DECRYPT_MODE, key, bytes);
	}
	
	public static byte[] encryptString(String key, String inputString) 
			throws CryptoException {
		return doCryptoString(Cipher.ENCRYPT_MODE, key, inputString.getBytes());
	}
	
	public static byte[] doCryptoString(int cipherMode, String key, byte[] inputBytes) throws CryptoException {
		try {
			Key secretKey = new SecretKeySpec(key.getBytes(), ALGORITHM);
			Cipher cipher = Cipher.getInstance(TRANSFORMATION);
			cipher.init(cipherMode, secretKey);
			byte[] data = cipher.doFinal(inputBytes);
			return data;
        } catch (NoSuchPaddingException | NoSuchAlgorithmException
                | InvalidKeyException | BadPaddingException
                | IllegalBlockSizeException ex) {//| UnsupportedEncodingException ex) {
            throw new CryptoException("Error encrypting/decrypting String", ex);
        }
	}
 
    private static void doCryptoFile(int cipherMode, String key, File inputFile,
            File outputFile) throws CryptoException {
        try {
            Key secretKey = new SecretKeySpec(key.getBytes(), ALGORITHM);
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(cipherMode, secretKey);
             
            FileInputStream inputStream = new FileInputStream(inputFile);
            byte[] inputBytes = new byte[(int) inputFile.length()];
            inputStream.read(inputBytes);
             
            byte[] outputBytes = cipher.doFinal(inputBytes);
             
            FileOutputStream outputStream = new FileOutputStream(outputFile);
            outputStream.write(outputBytes);
             
            inputStream.close();
            outputStream.close();
             
        } catch (NoSuchPaddingException | NoSuchAlgorithmException
                | InvalidKeyException | BadPaddingException
                | IllegalBlockSizeException | IOException ex) {
            throw new CryptoException("Error encrypting/decrypting file", ex);
        }
    }
}