package message;

import java.io.Serializable;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import javax.crypto.*;

/**
 * Class for adding timestamp to messages as well as newline character necessary to display different messages on different lines
 */
public class Message implements Serializable {

    //should be final
    private String message;

    /**
     * Prepends the current time to the string and stores it.
     * @param message given String
     */
    public Message(String message) {
        String time;
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH:mm:ss");
        LocalDateTime now = LocalDateTime.now();
        time = "<" + dtf.format(now) + "> ";
        this.message = time + message+ "\n";
    }

    /**
     * This class will be used when the client sends encrypted messages to the server
     * @param message
     * @param symmetricKey
     */
    public Message(String message, SecretKey symmetricKey) {
        String time;
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH:mm:ss");
        LocalDateTime now = LocalDateTime.now();
        time = "<" + dtf.format(now) + "> ";
        try {
            this.message = encrypt(time + message+"\n",symmetricKey);
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * This class will be used when the server sends the encrypted session key to the client
     * @param message
     * @param publicKey
     */
    public Message(String message,PublicKey publicKey) {
        try {
            this.message = encrypt(message,publicKey);
        } catch (Exception e){
            e.printStackTrace();
        }
    }


    public static String encrypt(String message, SecretKey key) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        // Create Cipher instance for symmetric encryption
        Cipher cipher = Cipher.getInstance("AES");

        // Initialize the Cipher for encryption with the symmetric key
        cipher.init(Cipher.ENCRYPT_MODE, key);

        // Encrypt the message
        byte[] encryptedBytes = cipher.doFinal(message.getBytes());

        // Encode the encrypted bytes as Base64 string and return
        return Base64.getEncoder().encodeToString(encryptedBytes);
    }

    public static String encrypt(String message, PublicKey publicKey) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        // Create Cipher instance for asymmetric encryption
        Cipher cipher = Cipher.getInstance("RSA");

        // Initialize the Cipher for encryption with public key
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);

        // Encrypt the message
        byte[] encryptedBytes = cipher.doFinal(message.getBytes());

        // Encode the encrypted bytes as Base64 string and return
        return Base64.getEncoder().encodeToString(encryptedBytes);
    }


    public String decrypt(SecretKey key) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {

        // Create Cipher instance for symmetric decryption
        Cipher cipher = Cipher.getInstance("AES");

        System.out.println(Base64.getEncoder().encodeToString(key.getEncoded()));
        // Initialize the Cipher for decryption with the symmetric key
        cipher.init(Cipher.DECRYPT_MODE, key);

        // Decode the Base64 string to get encrypted bytes
        byte[] encryptedBytes = Base64.getDecoder().decode(message);

        // Decrypt the message
        byte[] decryptedBytes = cipher.doFinal(encryptedBytes);

        // Return the decrypted string
        return new String(decryptedBytes);
    }


    public String decrypt(PrivateKey privateKey) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {

        // Create Cipher instance for asymmetric decryption
        Cipher cipher = Cipher.getInstance("RSA");

        // Initialize the Cipher for decryption with private key
        cipher.init(Cipher.DECRYPT_MODE, privateKey);

        // Decode the Base64 string to get encrypted bytes
        byte[] encryptedBytes = Base64.getDecoder().decode(message);

        // Decrypt the message
        byte[] decryptedBytes = cipher.doFinal(encryptedBytes);

        // Return the decrypted string
        return new String(decryptedBytes);
    }

    public String getMessagePart(){
        return this.message.substring(message.indexOf(">")+1);
    }

    public String toString(){
        return this.message;
    }

}