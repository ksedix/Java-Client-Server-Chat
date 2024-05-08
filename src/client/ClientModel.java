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
    private PrivateKey privateKey;
    private PublicKey publicKey;
    private SecretKey secretKey;

    //OBSERVER. ClientView observes the clientModel. The clientModel is OBSERVABLE
    private ClientView clientView;

    public static java.security.KeyPair generateKeyPair(){
        KeyPairGenerator keyPairGenerator = null;
        try {
            keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        keyPairGenerator.initialize(2048);
        return keyPairGenerator.generateKeyPair();
    }

    public ClientModel(){
        //Generate private and public keypair used for encryption/decryption
        KeyPair keyPair = generateKeyPair();
        this.publicKey = keyPair.getPublic();
        this.privateKey = keyPair.getPrivate();
    }

    public void connect(String username, String serverAddress) throws IOException, ClassNotFoundException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
        clientSocket = new Socket(serverAddress,1234);
        this.username = username;
        objectOutputStream = new ObjectOutputStream(clientSocket.getOutputStream());
        objectInputStream = new ObjectInputStream(clientSocket.getInputStream());
        //The first thing the client writes to the server is their username
        //this is so that the server can add them to their online user list. This is not encrypted
        objectOutputStream.writeObject(username);
        //the client sends the server its public key
        objectOutputStream.writeObject(this.publicKey);
        //The server will respond with the encrypted session key which it will send to the client
        Message message = (Message) objectInputStream.readObject();
        String secretKey = message.decrypt(privateKey);
        //System.out.println(secretKey);
        this.secretKey = new SecretKeySpec(Base64.getDecoder().decode(secretKey),"AES");
        //System.out.println(Base64.getEncoder().encodeToString(this.secretKey.getEncoded()));
    }

    public void sendMessage(String message) throws IOException {
        objectOutputStream.writeObject(new Message(username+": "+message,secretKey));
    }

    public void readMessage() throws IOException, ClassNotFoundException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
        Object obj = objectInputStream.readObject();

        if (obj instanceof Message){
            Message message = (Message) obj;
            //decrypt the message and add the decrypted message to the messages array
            messages.add(message.decrypt(secretKey));
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
    //Observer pattern will be necessary because the clientmodel needs to notify the clientview when
    //it has read a new message. The client controller can not do that because the client controller does not know
    // when the client has read a new message from the server. Therefore the client model needs to notify the view.
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
