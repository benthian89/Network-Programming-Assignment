//
import java.net.*;
import java.io.*;

public class UDPServer {
	
	public static void main (String args[]) throws Exception 
	{
		//use DatagramSocket for UDP connection
		DatagramSocket s = new DatagramSocket(9001);
		byte[] inBuf = new byte[1000];			
		
		while (true)
		{
			DatagramPacket inPkt = new DatagramPacket(inBuf, inBuf.length);
			s.receive(inPkt);
			// convert content of packet into a string 
			FileOutputStream myFile = new FileOutputStream("Data/testrecieved.txt");	
			
			myFile.write(inPkt.getData(),inPkt.getOffset(),inPkt.getLength());				
			myFile.close();
			
			
			
//			String request = new String(inPkt.getData(), 0, 
	//			inPkt.getLength());

			// convert string to uppercase
			String reply = "File received!";
			
			// convert upper-case string into array of bytes (output
			// buffer)
			byte[] outBuf = new byte[1000];
			outBuf = reply.getBytes();

			// create reply packet using output buffer.
			// Note: dest address/port is retrieved from inPkt
			DatagramPacket outPacket = new DatagramPacket(
				outBuf, outBuf.length, inPkt.getAddress(),
				inPkt.getPort());

			// finally, send the packet
			s.send(outPacket);
		}
	}
}
