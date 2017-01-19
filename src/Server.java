import java.net.*;
import java.util.Collections;
import java.util.HashMap;
import java.util.Scanner;
import java.io.*;

public class Server {
	//static properties
	public static final String ANSI = "\u001B["; //ANSI escape code root to execute special commands on remote terminals.
	
	//static methods
	public static void main(String[] args){
		try(ServerSocket serverSocket = new ServerSocket(4444)){//should i switch to default 23? 4444
			Server server = new Server(); //create new Server object (this program)
			while(true){ //go forever
				try {
					System.out.println("Waiting for connection.");
					Socket clientSocket = serverSocket.accept(); //accept() is a blocking call, which will return when a client connects to the server
					System.out.println("Connected to " + clientSocket.getInetAddress().toString()); //output ip of client
					server.connect(clientSocket); //connect client to Server object.
				} catch (IOException e) {
					e.printStackTrace(); //if something goes wrong, print it.
				}
			}
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}
	
	//private properties
	private HashMap<String,Socket> clientMap;
	Socket secondary;

	//public methods
	public Server(){
		clientMap = new HashMap<String,Socket>(); //init clientmap (names and their corresponding sockets)
		findListener();
		spawnAdmin(); //spawn admin thread. only accessible from terminal or IDE.
		redundantServerListener();
	}
	
	private void findListener(){
		Runnable run = new Runnable(){
			@Override
			public void run(){
				try(DatagramSocket UDPServer = new DatagramSocket(4443)){
					DatagramPacket packet = new DatagramPacket(new byte[0],0);
					while(true){
						try {
							UDPServer.receive(packet);
							System.out.println("Recieved packet from " + packet.getAddress().toString() + ":" + packet.getPort());
							InetAddress address = packet.getAddress();
							DatagramPacket send = new DatagramPacket(new byte[0],0,address,4442);
							UDPServer.send(send);
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				} catch (SocketException e) {
					e.printStackTrace();
				}
			}
		};
		new Thread(run).start();
	}
	
	void send(String toSend) throws IOException{
		for(String add:Collections.synchronizedMap(clientMap).keySet()){ //print the text on all the clients
			Socket client = Collections.synchronizedMap(clientMap).get(add); //get client
			PrintWriter pw = new PrintWriter(client.getOutputStream());
			pw.println(toSend);
			pw.flush();
		}
		if(secondary == null) return;	
		PrintWriter pw = new PrintWriter(secondary.getOutputStream());
		pw.println(toSend);
		pw.flush();
	}
	
	void redundantServerListener(){
		Runnable run = new Runnable(){
			@Override
			public void run() {
				System.out.println("Waiting for secondary server connection.");
				try(ServerSocket secondServer = new ServerSocket(4445)){
					secondary = secondServer.accept();
					System.out.println("Redundant server connected.");
					BufferedReader in = new BufferedReader(new InputStreamReader(secondary.getInputStream()));
					String s;
					while((s = in.readLine()) != null){
						System.out.println("From Secondary: " + s);
						send(s);
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		};
		new Thread(run).start();
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
					clientMap.put(name, clientSocket); //add client to the map
					System.out.println(name);
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

	//actions
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
		try {
			send("\n\nThe server is shutting down for now. Please try to join again later.");
		} catch (IOException e) {
			e.printStackTrace();
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
}