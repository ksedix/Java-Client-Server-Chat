package server;

import javax.swing.*;
import java.awt.*;

public class ServerView {
    private JPanel serverPanel;
    private JTextArea chatArea;
    private JTextArea onlineUsersArea;
    private JButton shutDownButton;
    private JButton startServerButton;
    private JButton loadChatButton;
    private JButton saveChatButton;

    private ServerModel serverModel;

    public ServerView(ServerModel serverModel){
        this.serverModel = serverModel;
        serverPanel.setPreferredSize(new Dimension(450,500));
    }

    public JPanel getServerPanel(){
        return serverPanel;
    }

    public JButton getSaveChatButton(){
        return saveChatButton;
    }

    public JButton getLoadChatButton() {
        return loadChatButton;
    }

    public JButton getShutDownButton() {
        return shutDownButton;
    }

    public JButton getStartServerButton() {
        return startServerButton;
    }

    public void updateUsers() {
        onlineUsersArea.setText("");
        for (String user : serverModel.getOnlineUsers()){
            onlineUsersArea.append(user);
        }
    }

    public void updateMessages() {
        chatArea.append(serverModel.getLatestMessage());
    }

    public void clearChat(){
        chatArea.setText("");
    }

}
