import java.net.*;
public class LANTest {
	public static void main(String[] args) throws Exception {
		  try{
			  byte[] toSend = "Hello World!".getBytes();
			  DatagramPacket packet = new DatagramPacket(toSend,toSend.length,InetAddress.getByName("localhost"),4443);
			  DatagramSocket socket = new DatagramSocket();
			  socket.send(packet);
			  System.out.println(new String(packet.getData()) + " sent to " + packet.getAddress().toString());
			  socket.close();
		  }
		  catch(Exception e){
			  e.printStackTrace();
		  }
	}
}
