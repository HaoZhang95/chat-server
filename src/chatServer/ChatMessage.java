package chatServer;

public class ChatMessage {
	
	private String fromWho;
	private String message;
	private String time;
	
	public ChatMessage(String fromWho,String message,String time) {
		// TODO Auto-generated constructor stub
		this.fromWho = fromWho;
		this.message = message;
		this.time = time;
	}
	
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return fromWho + " : " + message + " ( " + time + " )";
	}
}
