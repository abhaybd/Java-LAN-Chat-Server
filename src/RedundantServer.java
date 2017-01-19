import java.net.*;
import java.util.Collections;
import java.util.HashMap;
import java.util.Scanner;
import java.io.*;

public class RedundantServer {
	public static final String ANSI = "\u001B[";
	public static final String MAIN_SERVER = "10.49.109.54";
	public static void main(String[] args){
		RedundantServer server = new RedundantServer();
		try(ServerSocket serverSocket  = new ServerSocket(4443)){
			while(true){
				System.out.println("Waiting for connection.");
				Socket socket = serverSocket.accept();
				System.out.println("Connection recieved from: " + socket.getInetAddress().toString());
				server.connect(socket);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private HashMap<String, Socket> clientMap;
	private Socket mainServer;
	private boolean redundant;
	
	public RedundantServer(){
		clientMap = new HashMap<String, Socket>();
		try {
			//redundancyListener();
			mainServer = new Socket(MAIN_SERVER,4445);
			messageListener();
			redundant = true;
		} catch (IOException e) {
			//e.printStackTrace();
			redundant = false;
		}
		System.out.println(redundant);
		spawnAdmin();
	}
	
	void redundancyListener(){
		Runnable run = new Runnable(){
			@Override
			public void run(){
				while(true){
					try (Socket s = new Socket(MAIN_SERVER, 4445)){
						redundant = true;
					} catch (IOException e) {
						redundant = false;
					}
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		};
		new Thread(run).start();
	}
	
	void messageListener(){
		Runnable run = new Runnable(){
			@Override
			public void run() {
				try {
					BufferedReader in = new BufferedReader(new InputStreamReader(mainServer.getInputStream()));
					String s;
					while((s = in.readLine()) != null){
						for(String name:clientMap.keySet()){
							//System.out.println(s);
							PrintWriter out = new PrintWriter(Collections.synchronizedMap(clientMap).get(name).getOutputStream());
							out.println(s);
							out.flush();
						}
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		};
		new Thread(run).start();
	}
	
	void send(String toSend){
		try{			
			if(redundant){
				PrintWriter pw = new PrintWriter(mainServer.getOutputStream());
				pw.println(toSend);
				pw.flush();
				//System.out.println(toSend);
			}
			else{
				for(String name:clientMap.keySet()){
					PrintWriter out = new PrintWriter(clientMap.get(name).getOutputStream());
					out.println(toSend);
					out.flush();
				}
			}
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}
	
	private void spawnAdmin(){
		Runnable run = new Runnable(){ //create new runnable, to be run on a seperate thread.
			@Override
			public void run() {
				try(Scanner in = new Scanner(System.in)){
					while(true){
						String response = in.nextLine(); //get input from console/IDE. NOT from remote telnets.power
						if(response.toLowerCase().contains("kick")){ //kick users
							kick(response);
						}
						if(response.equals("quit")){//shutdown server
							quit();
						}
					}
				}
				catch(Exception e){
					e.printStackTrace();
				}
			}
		};
		new Thread(run).start(); //start the admin thread
	}

	private void quit(){
		for(String name:Collections.synchronizedMap(clientMap).keySet()){ //iterate through all clients and output shutdown message
			try{
				Socket socket = Collections.synchronizedMap(clientMap).get(name);
				PrintWriter out = new PrintWriter(socket.getOutputStream());
				out.println("\n\nThe server is shutting down for now. Please try to join again later.");
				out.flush();
				socket.close();
			}
			catch(Exception e){
				
			}
		}
		System.exit(0); //close down the program
	}

	private void kick(String response) throws IOException{
		String toKick = response.split(" ")[1];
		String msg = response.substring("kick ".length() + toKick.length());
		if(Collections.synchronizedMap(clientMap).containsKey(toKick)){
			if(response.split(" ").length > 2) {
				PrintWriter out = new PrintWriter(Collections.synchronizedMap(clientMap).get(toKick).getOutputStream());
				out.println("\nMessage from God: " + msg);
				out.flush();
			}
			Collections.synchronizedMap(clientMap).get(toKick).close();
			Collections.synchronizedMap(clientMap).remove(toKick);
		}
		System.out.println("Kicked " + toKick);
	}
	
	public void connect(Socket clientSocket){
		Runnable run = new Runnable(){ //create runnable, which will run on separate thread for this user. Each user wil have their own thread
			@Override
			public void run() {
				String name = "Guest"; //default name
				try {
					//get the input and output streams
					BufferedReader input = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
					PrintWriter out = new PrintWriter(clientSocket.getOutputStream());
					out.print(ANSI + "0;32;40m");//change the theme to black background and green text
					out.flush();
					do{
						out.println("Welcome to the L33T_H4XXORZ server! What is your nickname? "
								+ "(No spaces, must be less than 20 characters)");//print out prompt
						out.flush();
						name = input.readLine();//read result
					}while(name.length() == 0 || name.contains(" ") || name.length() > 20);//do until valid name
					out.print(ANSI + "2J"); //clear screen
					out.flush();
					out.println("Connected. Your username is: " + name);
					out.flush();
					Collections.synchronizedMap(clientMap).put(name, clientSocket);
					System.out.println(name);
					send(name + " has joined.");
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				try {
					BufferedReader in = new BufferedReader(new InputStreamReader(
					        clientSocket.getInputStream())); //get input stream
					String s = null;
					while((s = in.readLine()) != null){ //in.readLine() is a blocking call, and won't return until it gets input
						s = s.replaceFirst("^\\s+", "");//scrub leading spaces
						if(s.length() == 0 || s.length() > 140) continue;//not blank, and not too long
						send(name + " : " + s);
					}
				} catch (IOException e) {
					//e.printStackTrace();
				}
			}
		};
		new Thread(run).start(); //start client thread
	}
}
