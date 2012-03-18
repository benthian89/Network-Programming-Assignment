//
import java.net.*;
import java.io.*;
import javax.swing.*;
import java.util.*;
import java.util.Timer;
import java.util.TimerTask;

public class UDPClient {
	
	public static void main (String args[]) throws Exception {
		
		int Sn = 0;
		boolean canSend = true;
		Queue<byte[]> queue;
		ACKTimer t;
		
		//Use DataGramSocket for UDP connection
		DatagramSocket s = new DatagramSocket();
		
		//Addr and port number created
		InetAddress addr = InetAddress.getByName("localhost");
		int port = 9001;
		
		//Lets the user choose the file
		try {
			UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
		} catch (Exception e) {
			System.out.println("Unable to load Windows look and feel");
		}
		JFileChooser chooser = new JFileChooser(".");
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
     			
     			queue = new LinkedList<byte[]>();
     			queue.offer(outBuf);
     			
     			// create a packet buffer to store data from packets received.
     			byte inBuf[] = new byte[1000];
     			DatagramPacket inPkt = new DatagramPacket(inBuf, inBuf.length);
     			
     			while (!queue.isEmpty()) {
     				if (canSend) {
     					// Now create a packet (with destination addr and port)
     	     			// Sends over the actual file
     					byte[] frame = new byte[1000];
     					frame = queue.peek();
     	     			DatagramPacket outPkt = new DatagramPacket(frame, frame.length, addr, port);
     	     			s.send(outPkt);
     	     			t = new ACKTimer(2); // 2 second timeout
     	     			Sn++;
     	     			canSend = false;
     				}
     				
     				// receive the reply from server.
         			s.receive(inPkt);
     				if (new String(inPkt.getData(), 0, inPkt.getLength()) == "ACK") {
     					t.StopTimer();
     					queue.poll();
     					canSend = true;
     				}
     				
     				if (t.isTimeOut()) {
     					byte[] frame = new byte[1000];
     					frame = queue.peek();
     	     			DatagramPacket outPkt = new DatagramPacket(frame, frame.length, addr, port);
     	     			s.send(outPkt);
     	     			t = new ACKTimer(2); // 2 second timeout
     				}
     			}

     			/*
     			// convert reply to string and print to System.out
     			String reply2 = new String(inPkt.getData(), 0, inPkt.getLength());
     			System.out.println(reply2);
     			*/
     		}
     		else {
     			System.out.println("No acknowledgement from server that filename is received");
     			System.exit(0);
     		}
	}
	
	public class ACKTimer {
		private Timer timer;
		private boolean timeOut;
		
		public ACKTimer(int sec) {
			timeOut = false;
			timer = new Timer();
			timer.schedule(new ACKTask(), sec*1000);
		}
		
		public boolean isTimeOut() {
			return timeOut;
		}
		
		public void StopTimer() {
			timer.cancel();
		}
		
		public class ACKTask extends TimerTask {
			public void run() {
				timeOut = true;
				timer.cancel();
			}
		}
	}
}