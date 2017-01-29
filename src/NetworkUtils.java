import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;

public class NetworkUtils {
	public static Subnet getSubnet(){
		try {
			Process p = Runtime.getRuntime().exec("ipconfig");
			BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));
			String s;
			int[] ip = new int[4];
			int[] mask = new int[4];
			while((s = in.readLine()) != null){
				if(s.contains("IPv4 Address")){
					String[] parts = s.split(":")[1].replaceAll("[^\\d.]", "").split("\\.");
					System.out.println(s);
					for(int i = 0; i < parts.length; i++){
						System.out.print(parts[i] + ((i == parts.length - 1)?"":"."));
						ip[i] = Integer.parseInt(parts[i]);
					}
					System.out.print("\n");
				}
				if(s.contains("Subnet Mask")){
					
					String[] parts = s.split(":")[1].replaceAll("[^\\d.]", "").split("\\.");
					for(int i = 0; i < parts.length; i++){
						mask[i] = Integer.parseInt(parts[i]);
						System.out.print(mask[i] + ((i == parts.length - 1)?"":"."));
					}
					System.out.print("\n");
				}
			}
			int[] minIp = new int[4];
			int[] range = new int[4];
			for(int i = 0; i < minIp.length; i++){
				minIp[i] = toInt((byte) (ip[i] & mask[i]));
				range[i] = toInt((byte) ~(mask[i]));
				System.out.print(range[i] + ((i == minIp.length-1)?"":"."));
			}
			InetAddress a = InetAddress.getByName(String.format("%1$d.%2$d.%3$d.%4$d", minIp[0],minIp[1],minIp[2],minIp[3]));
			return new Subnet(a, range);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public static class Subnet{
		public InetAddress minIp;
		public int[] range;
		public Subnet(InetAddress minIp, int[] range){
			this.minIp = minIp;
			this.range = range;
		}
	}
	
	private static int toInt(byte b){
		int i = b;
		return (i+256)%256;
	}
}
