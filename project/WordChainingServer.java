import java.net.*; 
import java.io.*; 

public class WordChainingServer implements Runnable {
	final static int PLAYERNUM = 2;
	private WordChaingServerRunnable clients[] = new WordChaingServerRunnable[PLAYERNUM];
	static String wordList = "";
	public int clientCount = 0;

	private int ePort = -1;

	public WordChainingServer(int port) {
		this.ePort = port;
	}

	public void run() {
		ServerSocket serverSocket = null;
		try {
			serverSocket = new ServerSocket(ePort);
			System.out.println ("Server started: socket created on " + ePort);
			
			while (true) {
				addClient(serverSocket);
			}
			
		} catch (BindException b) {
			System.out.println("Can't bind on: "+ePort);
		} catch (IOException i) {
			System.out.println(i);
		} finally {
			try {
				if (serverSocket != null) serverSocket.close();
			} catch (IOException i) {
				System.out.println(i);
			}
		}
	}
	
	public int whoClient(int clientID) {
		for (int i = 0; i < clientCount; i++)
			if (clients[i].getClientID() == clientID)
				return i;
		return -1;
	}
	
	public void putClient(int clientID, String msg) {
		for (int i = 0; i < clientCount; i++) {
			clients[i].out.println(msg);
		}
	}
	
	public void putClient(int clientID, String inputLine, String wordList) {
		for (int i = 0; i < clientCount; i++)
			if (clients[i].getClientID() == clientID) {
				System.out.println("작성자: "+clientID);
			} else {
				System.out.println("메시지 받는 사람: "+clients[i].getClientID());
				if (this.getPlayerState()) {
					clients[i].out.println(clientID + "님이 입력한 단어 : " + inputLine);
					clients[i].out.println("현재까지의 단어 : " + wordList);
				}
			}
	}
	
	public void addClient(ServerSocket serverSocket) {
		Socket clientSocket = null;
		
		if (clientCount < clients.length) { 
			try {
				clientSocket = serverSocket.accept();
				clientSocket.setSoTimeout(40000); // 1000/sec
			} catch (IOException i) {
				System.out.println ("Accept() fail: "+i);
			}
			clients[clientCount] = new WordChaingServerRunnable(this, clientSocket);
			new Thread(clients[clientCount]).start();
			clientCount++;
			System.out.println ("Client connected: " + clientSocket.getPort()
					+", CurrentClient: " + clientCount);
		} else {
			try {
				Socket dummySocket = serverSocket.accept();
				WordChaingServerRunnable dummyRunnable = new WordChaingServerRunnable(this, dummySocket);
				new Thread(dummyRunnable);
				dummyRunnable.out.println(dummySocket.getPort()
						+ " < Sorry maximum user connected now");
				System.out.println("Client refused: maximum connection "
						+ clients.length + " reached.");
				dummyRunnable.close();
			} catch (IOException i) {
				System.out.println(i);
			}	
		}
	}
	
	/**
	 * 모든 플레이어가 READY상태인지 판단하는 함수
	 * @return true: 모든 플레이어 READY 누른 경우 / false: 한명이라도 READY 안누른 경우
	 */
	public boolean getPlayerState() {
		int readyCount = 0;
		
		for (int i = 0; i < clientCount; i++) {
			if (clients[i].isReady == true) 
				readyCount++;
		}
		
		if (readyCount == PLAYERNUM) return true;
		else return false;
	}
	
	public synchronized void delClient(int clientID) {
		int pos = whoClient(clientID);
		WordChaingServerRunnable endClient = null;
	      if (pos >= 0) {
	    	   endClient = clients[pos];
	    	  if (pos < clientCount-1)
	    		  for (int i = pos+1; i < clientCount; i++)
	    			  clients[i-1] = clients[i];
	    	  clientCount--;
	    	  System.out.println("Client removed: " + clientID
	    			  + " at clients[" + pos +"], CurrentClient: " + clientCount);
	    	  endClient.close();
	      }
	}
	
	public static void main(String[] args) throws IOException {
		if (args.length != 1) {
			System.out.println("Usage: Classname ServerPort");
			System.exit(1);
		}
		int ePort = Integer.parseInt(args[0]);
		
		new Thread(new WordChainingServer(ePort)).start();
	}
}

class WordChaingServerRunnable implements Runnable {
	protected WordChainingServer chatServer = null;
	protected Socket clientSocket = null;
	protected PrintWriter out = null;
	protected BufferedReader in = null;
	public int clientID = -1;
	public boolean isReady = false;
	public boolean isGameStarted = false;
	
	public WordChaingServerRunnable (WordChainingServer server, Socket socket) {
		this.chatServer = server;
		this.clientSocket = socket;
		clientID = clientSocket.getPort();
		try {
			out = new PrintWriter(clientSocket.getOutputStream(), true);
			in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
		} catch (IOException e) {}
	}
	
	public void run() {
		try {
			String inputLine;
			final String readyMsg = "님이 READY를 눌렀습니다.";
			final String unReadyMsg = "님이 UNREADY를 눌렀습니다.";
			final String startMsg = "끝말잇기 게임을 시작합니다.";
			final String terminateMsg = "님이 게임을 종료했습니다.";

			while ((inputLine = in.readLine()) != null) {
				if (inputLine.equalsIgnoreCase("Bye.")) {
					chatServer.putClient(getClientID(), getClientID() + terminateMsg);
					chatServer.delClient(getClientID());
					break;
				}
				
				if (inputLine.equalsIgnoreCase("READY")) {
					this.isReady = !this.isReady;
					if (this.isReady == true) 
						chatServer.putClient(getClientID(), getClientID() + readyMsg);
					else 
						chatServer.putClient(getClientID(), getClientID() + unReadyMsg);
				}
				
				if (chatServer.getPlayerState()) {
					/**
					 * 마지막 PLAYER가 "READY"를 입력한 경우 wordList에 "READY"문자가 들어가는 것을 막기 위해 continue사용
					 * (...정규표현식으로 좀 더 깔끔하게 바꿀 수 있지 않을까...)
					 */
					if (inputLine.equalsIgnoreCase("READY")) {
						isGameStarted = true;
						chatServer.putClient(getClientID(), startMsg);
						continue;
					}
						
					WordChainingServer.wordList += inputLine + "->";
					chatServer.putClient(getClientID(), inputLine, WordChainingServer.wordList);
				}
			}
		} catch (SocketTimeoutException ste) {
			System.out.println("Socket timeout occurred, force close() : " + getClientID());
			chatServer.delClient(getClientID());
		} catch (IOException e) {
			chatServer.delClient(getClientID());
		}
	}
	
	public int getClientID() {
		return clientID;
	}
	
	public void close() {
		try {
			if (in != null) in.close();
			if (out != null) out.close();
			if (clientSocket != null) clientSocket.close();
		} catch (IOException i) {}
	}
}
