import java.io.*;
import java.net.*;

public class Client {
	public static final int FIND_PORT = 4443;
	public static final int PORT = 4444;
	/*
	public static void main(String[] args){
		Client client = new Client();
		System.out.println("Scanning submask for server...");
		try {
			String ip = getServer();
			System.out.println("Server found: " + ip);
			client.connect(ip);
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	*/
	
	public static String getServer() throws IOException{
		Thread t = sendPackets();
		try(DatagramSocket socket = new DatagramSocket(4442)){
			DatagramPacket packet = new DatagramPacket(new byte[0],0);
			socket.receive(packet);
			return packet.getAddress().toString().replaceAll("[^\\d.]", "");
		} catch (IOException e) {
			e.printStackTrace();
		}
		finally{
			t.interrupt();
		}
		
		return null;
	}
	
	private static Thread sendPackets(){
		Runnable run = new Runnable(){
			@Override
			public void run() {
				try (DatagramSocket socket = new DatagramSocket()){
					for(int i = 96; i <= 111; i ++){ //96 111
						for(int j = 0; j <= 255; j++){//0 255
							if(Thread.interrupted()) return;
							String ip = "10.49." + i + "." + j;//10.49
							System.out.println("Testing " + ip + ":" + FIND_PORT);
							InetAddress addr;
							try{
								addr = InetAddress.getByName(ip);
								DatagramPacket packet = new DatagramPacket(new byte[0],0,addr,FIND_PORT);
								socket.send(packet);
							} catch (IOException e) {
								e.printStackTrace();
							}
						}
					}
				} catch (SocketException e1) {
					e1.printStackTrace();
				}
			}
		};
		Thread t = new Thread(run);
		t.start();
		return t;
	}
	
	public ClientGUI gui;
	private Socket socket;
	
	public Client(ClientGUI gui){
		this.gui = gui;
	}
	
	public Client(){}
	
	public void connect(String ip){
		try{
			socket = new Socket(ip, PORT);
			listenerThread(socket.getInputStream());
			/*
			PrintWriter out = new PrintWriter(socket.getOutputStream());
			Scanner in = new Scanner(System.in);
			while(true){
				String response = in.nextLine();
				out.println(response);
				out.flush();
			}
			*/
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void close(){
		try {
			socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void send(String s){
		PrintWriter out;
		try {
			out = new PrintWriter(socket.getOutputStream());
			out.println(s);
			out.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	Thread listener;
	void listenerThread(InputStream stream) throws IOException{
		Runnable run = new Runnable(){
			@Override
			public void run(){
				BufferedReader in = new BufferedReader(new InputStreamReader(stream));
				String s;
				try {
					while((s = in.readLine()) != null && !Thread.interrupted()){
						display(s);
					}
				} catch (IOException e) {
					e.printStackTrace();
				}				
			}
		};
		listener = new Thread(run);
		listener.start();
	}
	
	void display(String s){
		gui.display(s.replace(Server.ANSI, ""));
	}
}
