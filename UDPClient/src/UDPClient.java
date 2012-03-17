//
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class UDPClient {
	
	public static void main (String args[]) throws Exception {

		//Use DataGramSocket for UDP connection
		DatagramSocket s = new DatagramSocket();
		
		// convert string "hello" to array of bytes, suitable for
		// creation of DatagramPacket
		Path path = Paths.get("Data/test.txt");		
		byte outBuf[] = Files.readAllBytes(path);

		// Now create a packet (with destination addr and port)
		InetAddress addr = InetAddress.getByName("localhost");
		int port = 9001;
		DatagramPacket outPkt = new DatagramPacket(outBuf, outBuf.length,
			 addr, port);
		s.send(outPkt);
		
		// create a packet buffer to store data from packets received.
		byte inBuf[] = new byte[1000];
		DatagramPacket inPkt = new DatagramPacket(inBuf, inBuf.length);

		// receive the reply from server.
		s.receive(inPkt);

		// convert reply to string and print to System.out
		String reply = new String(inPkt.getData(), 0, inPkt.getLength());
		System.out.println(reply);
	}
}
