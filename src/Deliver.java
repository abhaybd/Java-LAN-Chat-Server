import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.io.*;

public class Deliver {
	public static final int FIND_PORT = 4440;
	
	public Deliver(){
		
	}
	
	public void getPayload() throws IOException{
		String ip = getServer();
		try(Socket socket = new Socket(ip,3333)){
			InputStream in = socket.getInputStream();
			while(true){
				int b;
				ArrayList<Integer> content = new ArrayList<Integer>();
				while((b = in.read()) != -1){
					content.add(b);
				}
				char[] bytes = new char[content.size()];
				for(int i = 0; i < content.size(); i++){
					bytes[i] = (char)(byte)(int)content.get(i);
				}
				PrintWriter out = new PrintWriter("out.jar");
				out.write(bytes);
				out.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
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
							//ip = "127.0.0.1";
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
}
