package client;

import javax.swing.*;
import java.awt.*;

public class ClientView {
    private JPanel clientPanel;
    private JPanel connectPanel;
    private JTextField usernameField;
    private JTextField serverAddressField;
    private JButton connectButton;
    private JLabel errorMessage;
    private JPanel chatPanel;
    private JTextArea chatArea;
    private JTextArea onlineUsersArea;
    private JTextField messageField;
    private JButton sendButton;
    private JButton backButton;
    private JMenuBar menuBar;
    private JMenuItem saveMenuItem;
    private JMenuItem homeMenuItem;

    private ClientModel clientModel;

    public ClientView(ClientModel clientModel){
        this.clientModel = clientModel;
    }

    //GETTER METHODS

    public JPanel getClientPanel(){
        return clientPanel;
    }

    public String getUserName(){
        return usernameField.getText().trim();
    }

    public String getServerIPAddress(){
        if (serverAddressField.getText().isBlank() || serverAddressField.getText().equals("Default: Localhost")){
            return "localhost";
        } else {
            return serverAddressField.getText();
        }
    }

    public JButton getConnectButton(){
        return connectButton;
    }

    public JButton getBackButton(){
        return backButton;
    }

    public String getMessage(){
        return messageField.getText();
    }

    public JMenuBar getMenuBar(){
        return menuBar;
    }

    public JButton getSendButton(){
        return sendButton;
    }

    public JTextField getServerAddressField() {
        return serverAddressField;
    }

    public void showChat() {
        CardLayout cardLayout = (CardLayout) clientPanel.getLayout();
        cardLayout.show(clientPanel,"chat");
    }

    public void showHome() {
        CardLayout cardLayout = (CardLayout) clientPanel.getLayout();
        cardLayout.show(clientPanel,"home");
    }

    public void updateMessages() {
        chatArea.append(clientModel.getLatestMessage());
    }

    public void updateUserList() {
        onlineUsersArea.setText("");
        for (String user : clientModel.getOnlineUsers()){
            onlineUsersArea.append(user);
        }
    }

    public JTextField getMessageField(){
        return messageField;
    }

    public JMenuItem getSaveMenuItem(){
        return saveMenuItem;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage.setText(errorMessage);
    }

    public JMenuItem getHomeMenuItem() {
        return homeMenuItem;
    }
}
