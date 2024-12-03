package client;

import message.Message;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.swing.*;
import java.io.*;
import java.net.Socket;
import java.security.*;
import java.util.ArrayList;
import java.util.Base64;

/**
 * Stores all the data of our application such as the chat log between the client and the server as well
 * as a list of all the online users
 * Contains methods for communicating with the server: Methods for the client to send messages to the server
 * and methods for the client to read messages from the server
 */
public class ClientModel {

    //messages can't be null because otherwise we will not be able to add any elements to it
    //Initialize it to an empty arraylist
    private ArrayList<String> messages = new ArrayList<>();
    private ArrayList<String> onlineUsers = new ArrayList<>();
    private ObjectOutputStream objectOutputStream;
    private ObjectInputStream objectInputStream;
    private Socket clientSocket;
    private String username;

    //NEW FIELDS
    //The private and public RSA key will be used to securely exchange the session key with the server
    private PublicKey publicKey;
    private PrivateKey privateKey;
    private SecretKey sessionKey;

    private static KeyPair generateKeyPair(){
        //Create a new keypair generator that generates an RSA key pair
        KeyPairGenerator keyPairGenerator = null;
        try {
            keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        //Set the key size to 2048 bits, this will be enough for our 1-time use of the key
        keyPairGenerator.initialize(2048);
        return keyPairGenerator.generateKeyPair();
    }

    public ClientModel(){
        KeyPair keyPair = generateKeyPair();
        this.privateKey = keyPair.getPrivate();
        this.publicKey = keyPair.getPublic();
    }


    //OBSERVER. ClientView observes the clientModel. The clientModel is OBSERVABLE
    private ClientView clientView;

    public void connect(String username, String serverAddress) throws IOException, ClassNotFoundException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
        clientSocket = new Socket(serverAddress,1234);
        this.username = username;
        objectOutputStream = new ObjectOutputStream(clientSocket.getOutputStream());
        objectInputStream = new ObjectInputStream(clientSocket.getInputStream());
        //The first thing the client writes to the server is their username
        //this is so that the server can add them to their online user list
        objectOutputStream.writeObject(username);

        //write the public key to the server so that it can use it to encrypt the secret session key
        //we will then use the private key to decrypt the session key
        //the public key is not written in encrypted form, but this is not an issue since it can only be used
        //for encryption. Only the person with the private key(i.e. client) can decrypt messages encrypted with public key
        objectOutputStream.writeObject(this.publicKey);

        //Read the encrypted session key which is sent as a message from the server to the client
        //this is the first message that the server will send from the client
        Message encryptedSessionKey = (Message) objectInputStream.readObject();
        //Decrypt the message to obtain a string representation of the unencrypted session key
        String sessionKey = encryptedSessionKey.decrypt(this.privateKey);
        //Turn the String representation of the session key into a real AES session key that can
        //be used for encryption/decryption. Store it in a private field(very important)
        this.sessionKey = new SecretKeySpec(Base64.getDecoder().decode(sessionKey),"AES");

    }

    public void sendMessage(String message) throws IOException, IllegalBlockSizeException, NoSuchPaddingException, BadPaddingException, NoSuchAlgorithmException, InvalidKeyException {
        //send a message that is encrypted using the symmetric session AES session key
        objectOutputStream.writeObject(new Message(username+": "+message,this.sessionKey));
    }

    public void readMessage() throws IOException, ClassNotFoundException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
        Object obj = objectInputStream.readObject();
        if (obj instanceof Message){
            Message message = (Message) obj;
            //Decrypt all messages that the server sends to us(client) and add them to the client log in plain text
            //so that the client can read the messages
            messages.add(message.decrypt(this.sessionKey));
            clientView.updateMessages();
        } else {
            ArrayList<String> onlineUsers = (ArrayList<String>) obj;
            this.onlineUsers = onlineUsers;
            clientView.updateUserList();
        }
    }

    public void listenForMessages(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    while (true){
                        readMessage();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                } catch (NoSuchPaddingException e) {
                    e.printStackTrace();
                } catch (IllegalBlockSizeException e) {
                    e.printStackTrace();
                } catch (NoSuchAlgorithmException e) {
                    e.printStackTrace();
                } catch (BadPaddingException e) {
                    e.printStackTrace();
                } catch (InvalidKeyException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public void disconnect() {
        try {
            if (clientSocket.isConnected()) {
                clientSocket.close();
            }
            if (objectOutputStream != null) {
                objectOutputStream.close();
            }
            if (objectInputStream != null) {
                objectInputStream.close();
            }
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    public String getLatestMessage(){
        return messages.get(messages.size()-1);
    }

    public ArrayList<String> getOnlineUsers(){
        return onlineUsers;
    }
    //Observer pattern will be necessary because the ClientModel needs to notify the ClientView when
    //it has read a new message. The client controller can not do that because the client controller does not know
    // when the client has read a new message from the server. Therefore the Client Model needs to notify the view.
    public void addObserver(ClientView clientView){
        this.clientView = clientView;
    }


    public void saveChat() throws IOException {
        JFileChooser jFileChooser = new JFileChooser();

        if (jFileChooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION){
            File file = jFileChooser.getSelectedFile();
            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(file));
            for (String msg : messages){
                bufferedWriter.write(msg);
            }
            bufferedWriter.close();
        }
    }
}
