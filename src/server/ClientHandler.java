package server;

import message.Message;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Base64;

public class ClientHandler implements Runnable{

    private ObjectInputStream objectInputStream;
    private ObjectOutputStream objectOutputStream;
    private Socket clientSocket;
    private String username;
    //Can't be null, otherwise we will not be able to add items to it
    private static ArrayList<ClientHandler> clientHandlers = new ArrayList<>();
    private ServerModel serverModel;
    private SecretKey sessionKey;

    public ClientHandler(Socket clientConnection, ServerModel serverModel) throws IOException, ClassNotFoundException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, NoSuchAlgorithmException, InvalidKeyException {
        this.clientSocket = clientConnection;
        this.serverModel = serverModel;
        //the session key will be sent to the client
        this.sessionKey = serverModel.getSessionKey();

        clientHandlers.add(this);

        this.objectInputStream = new ObjectInputStream(clientSocket.getInputStream());
        this.objectOutputStream = new ObjectOutputStream(clientSocket.getOutputStream());

        //read the username that the client sends to the server, this is the first item that client sends
        this.username = (String) objectInputStream.readObject();
        //Read the public key that the client sends to the server. This is the second item that the client sends
        PublicKey publicKey = (PublicKey) objectInputStream.readObject();
        //write the secret session key to the client and encrypt it with the public key that you read from the client.
        objectOutputStream.writeObject(new Message(Base64.getEncoder().encodeToString(sessionKey.getEncoded()),publicKey));

        serverModel.addOnlineUser(username);
        serverModel.addMessage(new Message("SERVER: "+username+" has connected to the server"));

        broadCastMessage(new Message("SERVER: "+username+" has connected to the server"));
        broadCastUsers(serverModel.getOnlineUsers());


    }

    private void broadCastUsers(ArrayList<String> onlineUsers) throws IOException {
        for (ClientHandler clientHandler : clientHandlers){
            clientHandler.sendUsers(onlineUsers);
        }
    }

    public void sendUsers(ArrayList<String> onlineUsers) throws IOException {
        objectOutputStream.writeObject(onlineUsers);
        //reset() is necessary to prevent the objectOutputStream from "remembering" an old version of
        //the onlineUsers array that it has sent, since we will be sending it multiple times. We always want it to
        //use the newest, up-to-date version of the onlineUsers array
        objectOutputStream.reset();
    }

    private void broadCastMessage(Message message) throws IOException {
        for (ClientHandler clientHandler : clientHandlers){
            clientHandler.sendMessage(message);
        }
    }

    public void sendMessage(Message message) throws IOException {
        objectOutputStream.writeObject(message);
    }

    public void readMessage() throws IOException, ClassNotFoundException {
        while (true){
            Message message = (Message) objectInputStream.readObject();
            broadCastMessage(message);
            serverModel.addMessage(message);
        }
    }

    @Override
    public void run() {
        try {
            readMessage();
        } catch (IOException e) {
            closeConnection();
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            closeConnection();
            e.printStackTrace();
        }
    }

    private void closeConnection() {
        try {
            if (clientSocket.isConnected()) {
                clientSocket.close();
            }
            if (objectInputStream != null) {
                objectInputStream.close();
            }
            if (objectOutputStream != null) {
                objectOutputStream.close();
            }
            //remove the client handler
            clientHandlers.remove(this);
            //remove the username and update the server online user list
            serverModel.removeUser(username);
            serverModel.addMessage(new Message("SERVER: " + username + " has disconnected from the server"));
            broadCastMessage(new Message("SERVER: " + username + " has disconnected from the server"));
            broadCastUsers(serverModel.getOnlineUsers());
        } catch (IOException e){
            e.printStackTrace();
        }
    }

}
