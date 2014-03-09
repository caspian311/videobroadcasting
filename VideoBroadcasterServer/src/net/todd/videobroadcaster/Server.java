package net.todd.videobroadcaster;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
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
			handleConnection(serverSocket.accept());
		}
	}

	private void handleConnection(Socket socket) {
		System.out.println("Received connection");
		
		FileOutputStream fos = null;
		InputStream in = null;
		try {
			in = socket.getInputStream();
			fos = new FileOutputStream(createOutputFile());
			
			readInFile(fos, in);
		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
			try {
				fos.close();
				socket.close();
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
	}

	private void readInFile(FileOutputStream fos, InputStream in) throws IOException {
		byte[] buffer = new byte[1024];
		int totalFileSize = 0;
		
		int readSize = 0;
		while((readSize = in.read(buffer)) > 0) {
			fos.write(buffer);
			totalFileSize += readSize;
		}
		
		System.out.println("Finished writing " + totalFileSize + " to file");
	}

	private File createOutputFile() throws IOException {
		File outputFile = File.createTempFile("video", ".mp4");
		System.out.println("Writing to: " + outputFile.getAbsolutePath());
		return outputFile;
	}
}
