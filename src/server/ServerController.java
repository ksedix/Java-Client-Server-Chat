package server;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

/**
 * The ServerController deals with user input and connects the serverView and the serverModel together.
 * For example, when a user clicks on a button in the ServerView, the controller will call a method
 * in the serverModel.
 */
public class ServerController extends JFrame {

    private ServerModel serverModel;
    private ServerView serverView;

    public ServerController(){
        this.serverModel = new ServerModel();
        this.serverView = new ServerView(serverModel);

        this.serverModel.addObserver(serverView);

        serverView.getSaveChatButton().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    serverModel.saveChat();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        });

        serverView.getLoadChatButton().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    serverModel.loadChat();
                } catch (IOException ex) {
                    ex.printStackTrace();
                } catch (ClassNotFoundException ex) {
                    ex.printStackTrace();
        }
            }
        });

        serverView.getStartServerButton().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                startServer();
            }
        });

        serverView.getShutDownButton().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.out.println("Shut down the server...TODO");
            }
        });

        //JFRAME METHODS
        add(serverView.getServerPanel());
        pack();
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setTitle("Server");
        setVisible(true);

    }

    private void startServer() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                //We run this on a separate thread because it is a blocking operation
                //We want to be able to use the other buttons as well, so we need to run this on a
                //separate thread
                try {
                    serverModel.startServer();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
}
