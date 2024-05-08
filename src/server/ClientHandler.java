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
    private PublicKey publicKey;
    private SecretKey sessionKey;

    public ClientHandler(Socket clientConnection, ServerModel serverModel) throws IOException, ClassNotFoundException {
        this.clientSocket = clientConnection;
        this.serverModel = serverModel;
        //Q: Is there any other way of doing this?
        this.sessionKey = serverModel.getSecretKey();

        clientHandlers.add(this);

        this.objectInputStream = new ObjectInputStream(clientSocket.getInputStream());
        this.objectOutputStream = new ObjectOutputStream(clientSocket.getOutputStream());
        //the username is the first item sent so read it from the input stream
        this.username = (String) objectInputStream.readObject();
        //the public key is the second item sent so read it from the input stream
        this.publicKey = (PublicKey) objectInputStream.readObject();

        serverModel.addOnlineUser(username);
        serverModel.addMessage(new Message("SERVER: "+username+" has connected to the server"));

        //Send the encrypted session key to the user. Turn the session key into a string first so that it can be sent as a Message
        sendMessage(new Message(Base64.getEncoder().encodeToString(sessionKey.getEncoded()),publicKey));

        broadCastMessage(new Message("SERVER: "+username+" has connected to the server",sessionKey));
        //the broadcast online user list is not encrypted
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

    public void readMessage() throws IOException, ClassNotFoundException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
        while (true){
            Message message = (Message) objectInputStream.readObject();
            broadCastMessage(message);
            serverModel.addEncryptedMessage(message);
        }
    }

    @Override
    public void run() {
        try {
            readMessage();
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
        } finally {
            closeConnection();
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
