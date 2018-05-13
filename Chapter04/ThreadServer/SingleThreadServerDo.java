package ThreadServer;

import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalDateTime;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class SingleThreadServerDo {
	public static void main(String args[]) {

		if (args.length != 1) {
			System.out.println("Usage: Classname serverPort");
			System.exit(1);
		}
		
		int serverPort = Integer.parseInt(args[0]);
		
		SingleThreadServer server = new SingleThreadServer(serverPort);
		new Thread(server).start();
		
		try {
			Thread.sleep(30 * 1000);  // 1000 (1sec)
		} catch (InterruptedException e) {
			e.printStackTrace();  
		}
		server.stop();
	}
}

class SingleThreadServer implements Runnable {

	protected int          serverPort   = 0000;
	protected ServerSocket serverSocket = null;
	protected boolean      isStopped    = false;
	protected Thread       runningThread= null;
	   
	public SingleThreadServer(int port) {
		this.serverPort = port;
	}

	public void run(){
		synchronized(this) {
			this.runningThread = Thread.currentThread();
		}
		openServerSocket();
		while(! isStopped()) {
			Socket clientSocket = null;
			
			try {
				clientSocket = this.serverSocket.accept();
				System.out.println("Client connection accepted");
			} catch (IOException ie) {
				if(isStopped()) {
					return;
				}
				throw new RuntimeException("Error accepting client connection", ie);
			}
			try {
				processClientRequest(clientSocket);
			} catch (IOException ie) {
				//log exception and go on to next request.
			}
		}
	}

	private void processClientRequest(Socket clientSocket) throws IOException {
		InputStream input = clientSocket.getInputStream();
		OutputStream output = clientSocket.getOutputStream();
		LocalDateTime cur = LocalDateTime.now();
		output.write(("HTTP/1.1 200 OK\n\nSingleRunnable: " + "\r" + "SingelThreaded Server: : "+cur+"").getBytes());
		System.out.println("Request processed: " + cur);
		output.close();
		input.close();
	}

	private synchronized boolean isStopped() {
		return this.isStopped;
	}

	public synchronized void stop(){
		this.isStopped = true;
		try {
			this.serverSocket.close();
			System.out.println("Close serverSocket");
		} catch (IOException e) {
			throw new RuntimeException("Error closing server", e);
		}
	}

	private void openServerSocket() {
		try {
			this.serverSocket = new ServerSocket(this.serverPort);
			System.out.println("Open serverSocket and waiting");
		} catch (IOException e) {
			throw new RuntimeException("Cannot open port "+serverPort, e);
		}
	}
}

