package chatServer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

public class ChatHistory extends Observable{

	private static ChatHistory chatHistory = new ChatHistory();
	private List<ChatMessage> chatMessages = null;

    public static ChatHistory getInstance() {
        return chatHistory;
    }

    private ChatHistory() {
    	chatMessages = new ArrayList<>();
    }
	
	public void insert(ChatMessage message) {
		chatMessages.add(message);
		
		setChanged();	//目标的信息发生改变
		notifyObservers(message);	//通知所有的观察者历史记录添加了一条
	}
	
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		String list ="";
		
		for(ChatMessage chatMessage : chatMessages){
			list += chatMessage.toString() + "\n";
		}
		return list;
	}
		
}
