package client;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;

/**
 * The client controller will connect the client view and the client model.
 * When a user clicks on a button or interacts with the client view, it will call methods in the client model
 * and update the client view.
 * Ex: When a user clicks the send button to send a message in the client view, it will call the logic
 * method in the client model that actually sends the message to the server.
 */
public class ClientController extends JFrame {

    private ClientModel clientModel;
    private ClientView clientView;

    public ClientController(){
        this.clientModel = new ClientModel();
        this.clientView = new ClientView(clientModel);

        //Add the client view as observer to the client model
        clientModel.addObserver(clientView);

        clientView.getConnectButton().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String username = clientView.getUserName();
                String serverIPAddress = clientView.getServerIPAddress();
                if (username.isBlank()){
                    clientView.setErrorMessage("Please provide a valid username");
                } else {
                    try {
                        clientModel.connect(username,serverIPAddress);
                        clientModel.listenForMessages();
                        //System.out.println("Change to chatPanel");
                        clientView.showChat();
                        setTitle("Client- "+username);
                    } catch (IOException exception) {
                        clientView.setErrorMessage("Failed to connect to the server");
                        exception.printStackTrace();
                    }
                }

            }
        });

        clientView.getBackButton().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //System.out.println("Change to connect panel");
                clientModel.disconnect();
                clientView.showHome();
            }
        });

        clientView.getHomeMenuItem().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //System.out.println("Change to connect panel");
                clientModel.disconnect();
                clientView.showHome();
            }
        });

        clientView.getSendButton().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String message = clientView.getMessage();
                try {
                    clientModel.sendMessage(message);
                    //clear the text in the message field after a client has sent a message
                    clientView.getMessageField().setText("");
                } catch (IOException ex) {
                    ex.printStackTrace();
                    clientModel.disconnect();
                    clientView.showHome();
                }
            }
        });

        //Also allow the client to send messages by pressing enter on the keyboard
        clientView.getMessageField().addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                String message = clientView.getMessage();
                if (e.getKeyChar() == KeyEvent.VK_ENTER){
                    try {
                        clientModel.sendMessage(message);
                        clientView.getMessageField().setText("");
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        });

        clientView.getSaveMenuItem().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    clientModel.saveChat();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        });


        clientView.getServerAddressField().addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                if (clientView.getServerAddressField().getText().equals("Default: Localhost")){
                    clientView.getServerAddressField().setText("");
                    clientView.getServerAddressField().setForeground(Color.black);
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (clientView.getServerAddressField().getText().isBlank()){
                    clientView.getServerAddressField().setForeground(Color.gray);
                    clientView.getServerAddressField().setText("Default: Localhost");
                }
            }
        });

        //JFRAME Methods
        add(clientView.getClientPanel());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setJMenuBar(clientView.getMenuBar());
        setTitle("Client");
        //Pack() makes sure the JFrame gets the same size as the preferred size of the client view.
        pack();
        //Centers the JFrame at the center of the window
        setLocationRelativeTo(null);
        setVisible(true);

    }

}
