//CS2105 Programming Assignment Part A
//Group 36
//A0072967N Koh Zhi Kai
//A0073002B Thian Chang Yi Benjamin

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

		try {
			UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
		} catch (Exception e) {
			System.out.println("Unable to load Windows look and feel.");
		}

		//Allows the user to choose the file using GUI
		JFileChooser chooser = new JFileChooser(".");
		chooser.showOpenDialog(null);
		File myFile = chooser.getSelectedFile();
		String filename = myFile.getName();

		//Converts filename to byte for sending via DatagramPacket
		byte[] filenameByte = filename.getBytes();

		//Created a packet ( with destination addr and port )
		//Sends over the filename first
		DatagramPacket outFilename = new DatagramPacket(filenameByte, filenameByte.length, addr, port);

		s.send(outFilename);

		//Creates a packet buffer to store data from packets received.
		byte infilenameReply[] = new byte[1000];
		DatagramPacket infilename = new DatagramPacket(infilenameReply, infilenameReply.length);

		//Receives the reply from server.
		s.receive(infilename);

		//Converts reply to string and print to System.out
		String reply = new String(infilename.getData(), 0, infilename.getLength());

		if (reply.equalsIgnoreCase("Filename received.")) {
			System.out.println("Server: " + reply);

			//Convert the file into byteArray
			FileInputStream fis =  new FileInputStream(myFile);

			//Checks if the file is too large. Cannot send more than 2GB due to the limit of Integer.
			long length = myFile.length();
			if (length > Integer.MAX_VALUE) {
				System.out.println("File is too large. Unable to send.");
				System.exit(0);
			}
			
			System.out.println("Start transferring file..");

			/**************************************************************
			 * Our packets have a byte size of 65504
			 * 65500 for data only
			 * The first 4 extra bytes for
			 * 1. Counter for packet number
			 * 2. Flag to indicate if the packet is the last packet or not
			 * 3. Packet length (1)
			 * 4. Packet length (2)
			 * *************************************************************/

			// Total number of packets to send
			int packets_remaining;
			if((int)length%65500 == 0) {
				packets_remaining = ((int)length / 65500);
			}

			else
				packets_remaining = ((int)length / 65500) + 1;

			// Flag for indication of last packet
			int last_packet = -1;

			// 1 byte is used for the last packet flag
			byte last_packetByte;
			byte[] previousPacket = new byte[65504];
			int packet_number = 0;
			boolean canSend = true;
			ACKTimer t = new ACKTimer();
			int timeout_counter = 0;

			while ( packets_remaining > 0) {
				if (canSend) {
					byte[] outBuf = new byte[65504];
					packet_number++;
					
					/**** packet numbering will start from 1 ****/

					outBuf[0] = (byte) packet_number;
					outBuf[2] = (byte) 0;

					// Checks if the packet is the last packet
					if (packets_remaining == 1) {
						last_packet = 1;
						last_packetByte = (byte) last_packet;
						outBuf[1] = last_packetByte;
						outBuf[2] = (byte) (((int)length%65500 & 65280) >> 8);
						outBuf[3] = (byte) ((int)length%65500 & 255);
						fis.read(outBuf, 4, (int)length%65500);
					}

					else {
						last_packet = 0;
						last_packetByte = (byte) last_packet;
						outBuf[1] = last_packetByte;
						outBuf[2] = (byte) 65500;
						fis.read(outBuf,4,65500);
					}

					// Now create a packet (with destination addr and port)
					// Sends over the actual file
					DatagramPacket outPkt = new DatagramPacket(outBuf, outBuf.length, addr, port);
					s.send(outPkt);

					t = new ACKTimer(2); // 2 sec timeout
					canSend = false;
					previousPacket = outBuf;

				}

				//Receives the ACK from server.
				byte inACKbyte[] = new byte[1000];
				DatagramPacket inACK = new DatagramPacket(inACKbyte, inACKbyte.length);
				s.receive(inACK);

				String ACK = new String(inACK.getData(), 0, inACK.getLength());
				int integer_ACK = Integer.parseInt(ACK);

				//ACK received and ACK number is the next packet to send
				if (integer_ACK == packet_number+1) {
					t.StopTimer();
					canSend = true;
					packets_remaining--;
					timeout_counter = 0;
				}

				// If ACK is the previous packet number or time out occurs, retransmit the packet
				if (integer_ACK == packet_number || t.isTimeOut()) {
					DatagramPacket outPkt = new DatagramPacket(previousPacket, previousPacket.length, addr, port);
					s.send(outPkt);
					t = new ACKTimer(2); // 2 sec timeout
					canSend = false;
					timeout_counter++;
				}

				// If time out occurs 3 times, the client will stop transmission.
				if (timeout_counter > 3 ) {
					System.out.println("Time out has occurred 3 times, transmission will terminate now.");
					System.exit(0);
				}
			}

			// Creates a packet buffer to store final reply from server that the file is received
			byte inBuf[] = new byte[1000];
			DatagramPacket inPkt = new DatagramPacket(inBuf, inBuf.length);

			// receive the reply from server.
			s.receive(inPkt);

			// convert reply to string and print to System.out
			String reply2 = new String(inPkt.getData(), 0, inPkt.getLength());
			System.out.println("Server: " + reply2);
			System.out.println("File successfully transferred.");
			System.exit(0);
		}
		
		else {
			System.out.println("No acknowledgement from server that filename is received.");
			System.exit(0);
		}
	}
}