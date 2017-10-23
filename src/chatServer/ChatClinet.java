package chatServer;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;



public class ChatClinet implements Runnable{
	
	private static List<ChatClinet> CLIENTS = new ArrayList<ChatClinet>();
	private static Map<String, String> IP_NAME = new HashMap<>();
	private static Map<String, String> IP_PORT = new HashMap<>();
	private static final String COMMAND = "list_update";
	
	private Socket socket;
	private StringBuffer userlist = new StringBuffer(COMMAND);
	private DataOutputStream dos = null;
	private DataInputStream dis = null;
	private String clientName;

	public ChatClinet(Socket socket) {
		// TODO Auto-generated constructor stub
		this.socket = socket;
		try {
			dis = new DataInputStream(socket.getInputStream());
			dos = new DataOutputStream(socket.getOutputStream());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		try {
			while (true) {
				String readUTF = dis.readUTF();
				/**
				 * Based on various readUTF from client, we do different processing
				 */
				if (isCommand(readUTF) && (readUTF.startsWith("client_name") || readUTF.startsWith("updated_name"))) {	
					if (readUTF.startsWith("client_name")) {
						
						clientName = readUTF.split(",")[1];
						
						if (! isExist(clientName)) {
							IP_NAME.put(socket.getInetAddress().toString(), clientName);
							IP_PORT.put(socket.getInetAddress().toString(), socket.getPort()+"");
							dos.writeUTF("ok");
							dos.flush();
							System.out.println("New Client : ---> " + clientName );
							
							Iterator<Entry<String, String>> iterator = IP_NAME.entrySet().iterator();
							while (iterator.hasNext()) {
								Map.Entry<String,String> entry = (Map.Entry<String,String>) iterator.next();
								userlist.append( entry.getValue() + "," +entry.getKey() + ";");
							}
							for (ChatClinet chatClinet : CLIENTS) {
								chatClinet.dos.writeUTF(userlist.toString());
								System.out.println(userlist.toString());
							}
							
							userlist.delete(COMMAND.length(), userlist.length());
							
						}else {
							dos.writeUTF("name exits");
						}
					}else if (readUTF.startsWith("updated_name")) {
						String current_name = readUTF.split(",")[1];
						String updated_name = readUTF.split(",")[2];
						Iterator<Entry<String, String>> iterator = IP_NAME.entrySet().iterator();
						while (iterator.hasNext()) {
							Map.Entry<String,String> entry = (Map.Entry<String,String>) iterator.next();
							if (entry.getValue().equals(current_name)) {
								entry.setValue(updated_name);
							}
							userlist.append( entry.getValue() + "," +entry.getKey() + ";");
						}
						
						for (ChatClinet chatClinet : CLIENTS) {
							chatClinet.dos.writeUTF(userlist.toString());
							System.out.println(userlist.toString());
						}
						
						userlist.delete(COMMAND.length(), userlist.length());
					}
				}else if (isCommand(readUTF) && readUTF.startsWith("upload_voice")){    
					System.out.println(readUTF);

					String s = readUTF.split(",")[1];
					String username = readUTF.split(",")[2];
					long size = Long.parseLong(s);

					//use current time and random number to name the voice file
					//avoid thousands clients upload voice at the same millisecond, avoid produce duplicate voice name 
					File file = new File("C:\\metropolia\\Project\\ProjectAmr",
							(new Date().getTime()) + "R" + ((int)Math.random() * 100000000) + ".amr");
					FileOutputStream fout = new FileOutputStream(file);

					byte[] b1 = new byte[1024];
					int len = 0;
					long length = 0;
					while ((len = dis.read(b1)) != -1){
						length += len;
						fout.write(b1,0,len);
						if (length >= size){
							break;
						}
					}
					fout.close();
					
					ChatMessage chatMessage = new ChatMessage(clientName, file.getName(),Utils.getTime());
					ChatHistory.getInstance().insert(chatMessage);

					System.out.println("voice file upload successfully...,start transfering to other clients...");
					
					//================transfering to other clients=============================
					
					for (int i = 0; i < CLIENTS.size(); i++) {
						ChatClinet client = CLIENTS.get(i);
						if (! this.equals(client)) {
							try{
								if (file.exists()){
									client.dos.writeUTF("voice_download" + file.length());
									client.dos.flush();
									
									FileInputStream fin = new FileInputStream(file);
									int len1 = 0;
									byte b2[] = new byte[1024];
									while ((len1 = fin.read(b2)) != -1){
										client.dos.write(b2,0,len1);
										client.dos.flush();
									}
									fin.close();
									System.out.println("send: " + file.length() + " bytes to clients");
								}else{
									throw new Exception();
								}
							}catch(Exception e){
								e.printStackTrace();
							}
						}
					}
				}else if (isCommand(readUTF) && readUTF.startsWith("rating")){ 
					String rating = readUTF.split(",")[1];
					String fromWho = readUTF.split(",")[2];
					System.out.println("rating record: " + fromWho + " ---> " + rating);
				}else if (! isCommand(readUTF)) {	//if dis.read() returns a normal message then post it to others
					ChatMessage chatMessage = new ChatMessage(clientName, readUTF,Utils.getTime());
					ChatHistory.getInstance().insert(chatMessage);
					for (int i = 0; i < CLIENTS.size(); i++) {
						ChatClinet client = CLIENTS.get(i);
						if (! this.equals(client)) {
							client.dos.writeUTF("text_download"+ readUTF); 
						}
					}
				}
				//System.out.println(ChatHistory.getInstance().toString());
			}
		}catch(SocketException e){
			e.printStackTrace();
		}catch (EOFException e) {
			System.out.println(clientName + " log out!");
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				CLIENTS.remove(this);
				deleteFromServer();
				
				Iterator<Entry<String, String>> iterator = IP_NAME.entrySet().iterator();
				while (iterator.hasNext()) {
					Map.Entry<String,String> entry = (Map.Entry<String,String>) iterator.next();
					userlist.append( entry.getValue() + "," +entry.getKey() + ";");
				}
				
				for (ChatClinet chatClinet : CLIENTS) {
					chatClinet.dos.writeUTF(userlist.toString());
					System.out.println(userlist.toString());
				}
				userlist.delete(COMMAND.length(), userlist.length());
				
				if(dos != null) dos.close();
				if(dis != null) dis.close();
				if(socket != null) socket.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	/**
	 * when client socket meets exception, we use iterator to delete faulty client from the server maps
	 * iterator the map of ip_name and ip_port and delete
	 */
	private void deleteFromServer() {
		
		Iterator<Entry<String, String>> iterator = IP_NAME.entrySet().iterator();
		while(iterator.hasNext()){
			Entry<String, String> entry = iterator.next();
			String key = entry.getKey();
			if (key.equals(socket.getInetAddress().toString())) {
				iterator.remove();
			}
		}
		Iterator<Entry<String, String>> iterator1 = IP_PORT.entrySet().iterator();
		while(iterator1.hasNext()){
			Entry<String, String> entry = iterator1.next();
			String key = entry.getKey();
			if (key.equals(socket.getInetAddress().toString())) {
				iterator1.remove();
			}
		}
	}
	
	public boolean isCommand(String string){
		if (string.startsWith("client_name") || string.startsWith("upload_voice") 
				|| string.startsWith("rating") || string.startsWith("updated_name")) {
			return true;
		}
		return false;
	}
	
	public boolean isExist(String string){

		if (IP_NAME.containsValue(string)) {
			return true;
		}
		return false;
	}
	
	public Socket getSocket() {return socket;}
	public static List<ChatClinet> getChatClients() { return CLIENTS;}
}
