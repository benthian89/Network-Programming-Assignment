//
import java.net.*;
import java.io.*;

public class UDPServer {
	
	public static void main (String args[]) throws Exception 
	{
		//use DatagramSocket for UDP connection
		DatagramSocket s = new DatagramSocket(9001);
		byte[] inBuf = new byte[1000];	
		byte[] infilenameByte = new byte[1000];
		
		while (true)
		{
			
			DatagramPacket infilename = new DatagramPacket(infilenameByte, infilenameByte.length);
			s.receive(infilename);
			
			String filename = new String(infilename.getData());
			if (!filename.equals(null)) {
			
				String reply = "Filename received!";
			
				byte[] outfilenameByte = new byte[1000];
				outfilenameByte = reply.getBytes();

				// create reply packet using outfilename buffer.
				// Note: dest address/port is retrieved from infilename
				DatagramPacket outfilename = new DatagramPacket(
				outfilenameByte, outfilenameByte.length, infilename.getAddress(),
				infilename.getPort());

				s.send(outfilename);
													
			DatagramPacket inPkt = new DatagramPacket(inBuf, inBuf.length);
			s.receive(inPkt);
			// convert content of packet into a file
			FileOutputStream myFile = new FileOutputStream("Data/"+filename);	
			
			myFile.write(inPkt.getData(),inPkt.getOffset(),inPkt.getLength());				
			myFile.close();
			

			// Create reply
			String reply2 = "File received!";
					
			byte[] outBuf = new byte[1000];
			outBuf = reply2.getBytes();

			// create reply packet using output buffer.
			// Note: dest address/port is retrieved from inPkt
			DatagramPacket outPacket = new DatagramPacket(
				outBuf, outBuf.length, inPkt.getAddress(),
				inPkt.getPort());

			// finally, send the packet
			s.send(outPacket);
			
			}
			
			else {
				System.out.println("filename not received");
			}
			
		}
	}
}
