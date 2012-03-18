//
import java.net.*;
import java.io.*;
import javax.swing.*;

public class UDPClient {
	
	public static void main (String args[]) throws Exception {
		
		//Use DataGramSocket for UDP connection
		DatagramSocket s = new DatagramSocket();
		
		//Addr and port number created
		InetAddress addr = InetAddress.getByName("localhost");
		int port = 9001;
		
		//Lets the user choose the file
		JFileChooser chooser = new JFileChooser();
        chooser.showOpenDialog(null);
        File myFile = chooser.getSelectedFile();
        String filename = myFile.getName();
		
        //Converts filename to byte for sending via DatagramPacket
        byte[] filenameByte = filename.getBytes();
       
        //Created a packet ( with destination addr and port )
        //Sends over the filename first
        DatagramPacket outFilename = new DatagramPacket(filenameByte, filenameByte.length,
        		addr, port);
        
        s.send(outFilename);
        
     // create a packet buffer to store data from packets received.
     		byte infilenameReply[] = new byte[1000];
     		DatagramPacket infilename = new DatagramPacket(infilenameReply, infilenameReply.length);

     		// receive the reply from server.
     		s.receive(infilename);

     		// convert reply to string and print to System.out
     		String reply = new String(infilename.getData(), 0, infilename.getLength());
     		if (reply.equalsIgnoreCase("Filename received!")) {    			
     			System.out.println(reply);
     			//Convert the file into byteArray
     			FileInputStream fis =  new FileInputStream(myFile);
        
     			long length = myFile.length();
     			if (length > Integer.MAX_VALUE){
     				System.out.println("File is too large");
     				System.exit(0);
     			}
     			byte[] outBuf = new byte[(int)length]; 
		
     			fis.read(outBuf);

		
     			// Now create a packet (with destination addr and port)
     			// Sends over the actual file
     			DatagramPacket outPkt = new DatagramPacket(outBuf, outBuf.length,
     					addr, port);
     			s.send(outPkt);
		
     			// create a packet buffer to store data from packets received.
     			byte inBuf[] = new byte[1000];
     			DatagramPacket inPkt = new DatagramPacket(inBuf, inBuf.length);

     			// receive the reply from server.
     			s.receive(inPkt);

     			// convert reply to string and print to System.out
     			String reply2 = new String(inPkt.getData(), 0, inPkt.getLength());
     			System.out.println(reply2);
     		}
     		else {
     			System.out.println("No acknowledgement from server that filename is received");
     			System.exit(0);
     		}
	}
}


