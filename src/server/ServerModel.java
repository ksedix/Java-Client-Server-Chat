package server;

import message.Message;

import javax.crypto.*;
import javax.swing.*;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.InvalidKeyException;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

/**
 * The server model stores all the data of our application, such as all the messages that have been sent
 * between the clients and the server as well as a list of all online users
 */
public class ServerModel {

    private final int PORT_NUMBER = 1234;
    private ArrayList<String> messages = new ArrayList<>();
    private ArrayList<String> onlineUsers = new ArrayList<>();
    private ServerSocket serverSocket;
    private ServerView serverView;

    //NEW FIELD
    //The session key will be used to encrypt all regular messages between the client and the server
    //It needs to be securely shared with the client from the server
    //Hence, it needs to be encrypted with the clients public key and sent back to the client
    private SecretKey sessionKey;

    public ServerModel(){
        //Create a key generator that generates a symmetric AES key
        KeyGenerator keyGenerator = null;
        try {
            keyGenerator = KeyGenerator.getInstance("AES");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        //Initialize the key size to 256 bits
        keyGenerator.init(256);
        this.sessionKey = keyGenerator.generateKey();
    }

    /**
     * Getter so that ClientHandler can access the session key
     * @return
     */
    public SecretKey getSessionKey(){
        return sessionKey;
    }


    public void startServer() throws IOException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, NoSuchAlgorithmException, InvalidKeyException, ClassNotFoundException {
        serverSocket = new ServerSocket(PORT_NUMBER);
        addMessage(new Message("SERVER: "+"Server has been started and is listening for connections on port "+PORT_NUMBER));
        //blocking operation
        while (true){
            Socket clientConnection = serverSocket.accept();
            ClientHandler clientHandler = new ClientHandler(clientConnection,this);
            Thread thread = new Thread(clientHandler);
            thread.start();
        }

    }

    public void addOnlineUser(String username) {
        //The line break is necessary so that the users display on new lines in the online users text area.
        onlineUsers.add(username+"\n");
        serverView.updateUsers();
    }

    public void removeUser(String username){
        onlineUsers.remove(username+"\n");
        serverView.updateUsers();
    }

    public ArrayList<String> getOnlineUsers(){
        return onlineUsers;
    }

    public void addMessage(Message message) {
        if (message.isAnnouncement()) {
            messages.add(message.toString());
        } else {
            //this will make each encrypted message on a new line since they do not have new line character once they
            //are encrypted unless they are decrypted
            messages.add(message.toString()+"\n");
        }
        serverView.updateMessages();
    }

    public void addEncryptedMessage(Message encryptedMessage) throws NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
        messages.add(encryptedMessage.decrypt(this.sessionKey));
        serverView.updateMessages();
    }

    public String getLatestMessage(){
        return messages.get(messages.size()-1);
    }

    public void addObserver(ServerView serverView){
        this.serverView = serverView;
    }

    public void saveChat() throws IOException {
        JFileChooser jFileChooser = new JFileChooser();

        if (jFileChooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION){
            File file = jFileChooser.getSelectedFile();
            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(file));
            for (String msg : messages){
                bufferedWriter.write(msg.toString());
            }
            bufferedWriter.close();
        }
    }

    public void loadChat() throws IOException, ClassNotFoundException {
        JFileChooser jFileChooser = new JFileChooser();

        if (jFileChooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION){
            //when loading a chat log, we clear the messages array so that it only contains the messages in the loaded chat log
            messages = new ArrayList<>();
            //we have to clear the serverView as well, otherwise the old messages will not dissapear
            serverView.clearChat();
            File file = jFileChooser.getSelectedFile();
            BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
            String line = bufferedReader.readLine();
            while (line != null){
                //we can not add a message again as a message. this will put 2 timestamps on it
                //therefore we don't store Message class in the messages array, but simply string
                messages.add(line+"\n");
                serverView.updateMessages();
                line = bufferedReader.readLine();
            }
            bufferedReader.close();
        }
    }



}
