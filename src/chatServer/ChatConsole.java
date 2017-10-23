package chatServer;

import java.util.Observable;
import java.util.Observer;

public class ChatConsole implements Observer {

	@Override
	public void update(Observable o, Object arg) {
		// TODO Auto-generated method stub
		ChatHistory chatHistory = (ChatHistory) o;
		System.out.println(chatHistory.toString());
	}

}
