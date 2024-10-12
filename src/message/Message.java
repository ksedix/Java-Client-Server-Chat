package message;

import javax.crypto.*;
import java.io.Serializable;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;

/**
 * Class for adding timestamp to messages as well as newline character necessary to display different messages on different lines
 */
public class Message implements Serializable {

    private final String message;
    private boolean isSessionKey = false;
    private boolean announcement = false;

    /**
     * Prepends the current time to the string and stores it.
     * @param message given String
     */
    public Message(String message) {
        announcement = true;
        String time;
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH:mm:ss");
        LocalDateTime now = LocalDateTime.now();
        time = "<" + dtf.format(now) + "> ";
        this.message = time + message + "\n";
    }

    /**
     * This constructor is used to encrypt a message sent from the client to the server using the symmetric session key generated by the server
     * @param message
     * @param sessionKey A symmetric session key generated by the server and securely shared with the client
     */
    public Message(String message, SecretKey sessionKey) throws IllegalBlockSizeException, NoSuchPaddingException, BadPaddingException, NoSuchAlgorithmException, InvalidKeyException {
        String time;
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH:mm:ss");
        LocalDateTime now = LocalDateTime.now();
        time = "<" + dtf.format(now) + "> ";
        this.message = encrypt(time + message + "\n",sessionKey);
    }

    /**
     * This constructor is used to encrypt messages sent from the server to the client using the Public key generated by the client
     * This constructor is only used once by the server to encrypt the session key and share it securely with the client
     * @param message
     * @param publicKey An assymetric Public Key generated by the client and shared with the server
     */
    public Message(String message, PublicKey publicKey) throws NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, NoSuchAlgorithmException, InvalidKeyException {
        //the only thing you ever encrypt with a public key is the session key.
        this.isSessionKey = true;
        this.message = encrypt(message,publicKey);
    }

    /**
     * Helper function used to encrypt messages with a symmetric session key
     * @param message
     * @param sessionKey
     * @return
     */
    public static String encrypt(String message, SecretKey sessionKey) throws IllegalBlockSizeException, BadPaddingException, InvalidKeyException, NoSuchPaddingException, NoSuchAlgorithmException {
        //Create a new cipher instance that uses AES encryption(since our sessionKey is an AES key)
        Cipher cipher = Cipher.getInstance("AES");
        //Initialize the cipher to be used with encryption and to use our session key
        cipher.init(Cipher.ENCRYPT_MODE, sessionKey);
        //Encrypt the message(you have to provide the message bytes) and store the encrypted bytes
        byte[] encryptedBytes = cipher.doFinal(message.getBytes());
        //Convert the encrypted message bytes into its corresponding string representation so that it can be sent as a message
        return Base64.getEncoder().encodeToString(encryptedBytes);
    }

    /**
     * Helper function used to encrypt messages with an assymetric public key
     * @param message
     * @param publicKey
     * @return
     */
    public static String encrypt(String message, PublicKey publicKey) throws BadPaddingException, InvalidKeyException, NoSuchPaddingException, NoSuchAlgorithmException, IllegalBlockSizeException {
        //Create a new Cipher that uses RSA encryption(The reason is that our public key will be an RSA key)
        Cipher cipher = Cipher.getInstance("RSA");
        //Initialize the cipher to be used with encryption and to use our public key
        cipher.init(Cipher.ENCRYPT_MODE,publicKey);
        //Encrypt the message and store the encrypted message bytes
        byte[] encryptedBytes = cipher.doFinal(message.getBytes());
        //Convert the encrypted message bytes into corresponding encrypted string so that it can be sent as message
        return Base64.getEncoder().encodeToString(encryptedBytes);
    }

    /**
     * Instance method used to decrypt message and return decrypted message
     * @param sessionKey AES symmetric session key
     * @return
     */
    public String decrypt(SecretKey sessionKey) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        //Create a new Cipher instance that uses AES encryption
        Cipher cipher = Cipher.getInstance("AES");
        //Initialize the cipher to be used for decryption with the provided session key
        cipher.init(Cipher.DECRYPT_MODE,sessionKey);
        //Get the encrypted bytes back from the string message
        //Here you must use Base64.Decoder, since the bytes were encoded to string using Base64 encoder
        byte[] encryptedBytes = Base64.getDecoder().decode(this.message);
        //decrypt the encrypted bytes using cipher
        byte[] decryptedBytes = cipher.doFinal(encryptedBytes);
        //return the decrypted bytes as a string message
        //Here you CAN and SHOULD use the String constructor since the decrypted bytes can
        //be represented as a string.
        return new String(decryptedBytes);
    }

    /**
     * Instance method used to decrypt message and return decrypted message
     * This method will be used when the client decrypts the encrypted session key that the server sends to it
     * @param privateKey RSA assymetric private key
     * @return
     */
    public String decrypt(PrivateKey privateKey) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        //Create a new Cipher that uses RSA encryption
        Cipher cipher = Cipher.getInstance("RSA");
        //Initialize the cipher to be used with decryption and use the private RSA key
        cipher.init(Cipher.DECRYPT_MODE,privateKey);
        //Decode the String message to its corresponding encrypted bytes
        //Important to use Base64 decoder and not this.message.getBytes()
        //The reason is that the String has been encoded with Base64.encoder
        byte[] encryptedBytes = Base64.getDecoder().decode(this.message);
        //decrypt the encrypted bytes and store the decrypted bytes
        byte[] decryptedBytes = cipher.doFinal(encryptedBytes);
        //return the decrypted bytes as a regular String using the String constructor
        //Do not use Base64.encoder here. We want a regular string.
        return new String(decryptedBytes);
    }

    /**
     * Returns the encapsulated String message
     * @return String object message
     */
    public String toString() {
        return message;
    }

    public String getMessagePart(){
        return message.substring(message.indexOf(">")+1);
    }

    public boolean isSessionKey(){
        return this.isSessionKey;
    }

    public boolean isAnnouncement(){
        return announcement;
    }

}