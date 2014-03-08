package net.todd.videobroadcaster;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

import javax.net.ServerSocketFactory;

public class Server {
	private int port = 1234;
	
	public static void main(String[] args) throws Exception {
		new Server().start();
	}
	
	public void start() throws Exception {
		ServerSocket serverSocket = ServerSocketFactory.getDefault().createServerSocket(port);
		System.out.println("Listening on port " + port);
		while (true){
			System.out.println("Waiting for connection...");
			Socket socket = serverSocket.accept();
			System.out.println("Received connection");
			
			InputStream in = socket.getInputStream();
			
			File outputFile = File.createTempFile("video", ".mp4");
			System.out.println("Writing to: " + outputFile.getAbsolutePath());
			FileOutputStream fos = new FileOutputStream(outputFile);
			
			byte[] buffer = new byte[1024];
			int readSize = 0;
			int totalFileSize = 0;
			while((readSize = in.read(buffer)) > 0) {
				fos.write(buffer);
				totalFileSize += readSize;
			}
			fos.close();
			socket.close();
			System.out.println("Finished writing " + totalFileSize + " to file");
		}
	}
}
