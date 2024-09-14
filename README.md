# Java-Client-Server-Chat
A Client-Server chat application made in Java using Java Swing for front-end and Java Sockets for back-end.  
Tutorial for making the app: https://youtu.be/i-9FJizC3a8

## Preview

### Client
The client is first presented with a login window. The login window has 2 fields - the **username** of the client and the **server IP address** that the client wishes to connect to. The default Server IP Address is localhost - the clients own computer.  
<img src="https://user-images.githubusercontent.com/76788207/206730491-bba7fd61-4503-4b49-9e33-5344f4038541.png" alt="client login"/>

If the server is not running on the IP address specified by the client, the client will get an error message *"Failed to connect to Server"* when they click on the connect button.   
<img src="https://user-images.githubusercontent.com/76788207/206730499-42c5e037-c270-431b-9f90-325c88ab41b2.png" alt="Failed to connect to server"/>

If the server is running on the IP address specified by the client, the client will be presented with a chat window, after they click on the connect button, where they can see all the users that are connected to the same server and can chat with them. The image below shows 2 different clients talking to each other.
<img src="https://user-images.githubusercontent.com/76788207/206730485-ecc8f1d7-6f8b-45fb-aa7a-dadbc2dffd35.png" alt="chat window"/>

### Server
The server is made up of a single window with 4 buttons:  
**Save Chat**: Save the chat log to a text file   
**Load Chat**: Load the chat from a text file   
**Shut Down**: Shut down the server. All clients will be disconnected. (This function is not implemented yet)   
**Start Server**: Starts the Server. The server needs to be started in order for clients to be able to connect to it.   
<img src="https://user-images.githubusercontent.com/76788207/206730326-112fc10f-4671-4145-aa55-b1ea01f335c8.png" alt="Server window">

When we start the server, it will tell us that the server is listening for connections on port 1234. Now, clients can connect to it if they specify the correct Server IP address.  
<img src="https://user-images.githubusercontent.com/76788207/206730466-ff802cf5-90e2-4a0f-a3a9-501bd9a33d1c.png" alt="Server running">

## How To Run
There are 2 ways to run the application:
- Download **Client.jar** and **Server.jar** from the repository and run them.
- Clone the entire project and run **Client.java** to start the Client and **Server.java** to start the Server.
