package server;

import message.Message;

import javax.crypto.*;
import javax.swing.*;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Base64;

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
    private SecretKey secretKey;

    public ServerModel(){
        KeyGenerator keyGenerator = null;
        try {
            keyGenerator = KeyGenerator.getInstance("AES");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        // For AES, common key sizes are 128, 192, or 256 bits
        keyGenerator.init(256); // Initialize with a key size of 256 bits

        // Generate the symmetric key
        this.secretKey = keyGenerator.generateKey();
    }

    public void startServer() throws IOException, ClassNotFoundException {
        System.out.println(Base64.getEncoder().encodeToString(secretKey.getEncoded()));
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
        messages.add(message.toString());
        serverView.updateMessages();
    }

    public void addEncryptedMessage(Message message) throws NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
        messages.add(message.decrypt(secretKey));
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

    public SecretKey getSecretKey(){
        return this.secretKey;
    }

}
