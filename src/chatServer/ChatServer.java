package chatServer;

import java.io.IOException;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class ChatServer {
	
	private ServerSocket ss;
	
	
    public void serve() {
        try {
            ss = new ServerSocket(8888 , 10);

            while(true) {
                Socket s = ss.accept();
                
                ChatClinet client = new ChatClinet(s);
                new Thread(client).start();
                ChatClinet.getChatClients().add(client);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
 }