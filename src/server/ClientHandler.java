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

public class ClientHandler implements Runnable{

    private ObjectInputStream objectInputStream;
    private ObjectOutputStream objectOutputStream;
    private Socket clientSocket;
    private String username;
    //Can't be null, otherwise we will not be able to add items to it
    private static ArrayList<ClientHandler> clientHandlers = new ArrayList<>();
    private ServerModel serverModel;
    private SecretKey sessionKey;
    private ClientHandler keyReceiver;

    public ClientHandler(Socket clientConnection, ServerModel serverModel) throws IOException, ClassNotFoundException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, NoSuchAlgorithmException, InvalidKeyException {
        this.clientSocket = clientConnection;
        this.serverModel = serverModel;
        //the session key will be sent to the client
        //this.sessionKey = serverModel.getSessionKey();

        clientHandlers.add(this);

        this.objectInputStream = new ObjectInputStream(clientSocket.getInputStream());
        this.objectOutputStream = new ObjectOutputStream(clientSocket.getOutputStream());

        //read the username that the client sends to the server, this is the first item that client sends
        this.username = (String) objectInputStream.readObject();

        //Read the public key that the client sends to the server. This is the second item that the client sends
        //PublicKey publicKey = (PublicKey) objectInputStream.readObject();
        //write the secret session key to the client and encrypt it with the public key that you read from the client.
        //objectOutputStream.writeObject(new Message(Base64.getEncoder().encodeToString(sessionKey.getEncoded()),publicKey));
        serverModel.addOnlineUser(username);
        //serverModel.addMessage(new Message("SERVER: "+username+" has connected to the server"));
        broadCastUsers(serverModel.getOnlineUsers());

        Message announcement = new Message("SERVER: "+username+" has connected to the server");
        serverModel.addMessage(announcement);
        broadCastMessage(announcement);

        if (serverModel.getOnlineUsers().size() > 1) {
            // Only read public key from clients that are not the key warden
            //if client replies with their public key, this means they don't have a session key
            PublicKey publicKey = (PublicKey) objectInputStream.readObject();

            // Find the key warden
            ClientHandler keyWarden = getKeyWarden();
            if (keyWarden != null) {
                // Forward the new client's public key to the key warden
                keyWarden.setKeyReceiver(this);
                keyWarden.sendPublicKey(publicKey);
            }
        }
    }

    public void setKeyReceiver(ClientHandler clientHandler){
        this.keyReceiver = clientHandler;
    }

    private void sendPublicKey(PublicKey publicKey) throws IOException {
        objectOutputStream.writeObject(publicKey);
    }

    private ClientHandler getKeyWarden() {
        // Assuming the first client in the list is the key warden
        if (!clientHandlers.isEmpty()) {
            return clientHandlers.get(0);
        }
        return null;
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
        //These messages do not need to be encrypted with session key since they are already encrypted
        //The server simply receives encrypted message from client and broadcasts it to all other clients.
        objectOutputStream.writeObject(message);
    }

    public void readMessage() throws IOException, ClassNotFoundException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
        while (true){
            Message message = (Message) objectInputStream.readObject();
            System.out.println(message.isSessionKey());
            if (message.isSessionKey()){
                //if the message is a session key, we want to send it to the client that sent their public key
                keyReceiver.objectOutputStream.writeObject(message);
            } else {
                broadCastMessage(message);
                //we need to decrypt the message that server receives from client before we add it to server log
                //otherwise we can not read it
                serverModel.addMessage(message);
                //serverModel.addEncryptedMessage(message);
            }
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
        } catch (NoSuchPaddingException e) {
            closeConnection();
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            closeConnection();
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            closeConnection();
            e.printStackTrace();
        } catch (BadPaddingException e) {
            closeConnection();
            e.printStackTrace();
        } catch (InvalidKeyException e) {
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
            Message announcement = new Message("SERVER: " + username + " has disconnected from the server");
            serverModel.addMessage(announcement);
            broadCastMessage(announcement);
            broadCastUsers(serverModel.getOnlineUsers());
        } catch (IOException e){
            e.printStackTrace();
        }
    }

}
