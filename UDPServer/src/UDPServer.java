//CS2105 Programming Assignment Part A
//Group 36
//A0072967N Koh Zhi Kai
//A0073002B Thian Chang Yi Benjamin

import java.net.*;
import java.io.*;

public class UDPServer {

	public static void main (String args[]) throws Exception {
		//use DatagramSocket for UDP connection
		DatagramSocket s = new DatagramSocket(9001);

		byte[] infilenameByte = new byte[1000];
		FileOutputStream myFile = null;
		String filename;
		int packet_number = 1;

		// Receives the filename from Client
		DatagramPacket infilename = new DatagramPacket(infilenameByte, infilenameByte.length);
		s.receive(infilename);

		filename = new String(infilename.getData());

		// If filename is not empty, we create a file with the filename and send back ACK that filename is received
		if (!filename.equals(null)) {
			myFile = new FileOutputStream("Data/"+filename);
			String reply = "Filename received.";
			System.out.println(reply);

			byte[] outfilenameByte = new byte[1000];
			outfilenameByte = reply.getBytes();

			// Creates reply packet using outfilename buffer.
			// Note: dest address/port is retrieved from infilename
			DatagramPacket outfilename = new DatagramPacket(outfilenameByte, outfilenameByte.length, infilename.getAddress(), infilename.getPort());

			s.send(outfilename);
		}
		
		else {
			System.out.println("No filename received.");
			System.exit(0);
		}

		// Server waits for the actual data to arrive
		while (true) {
			byte[] inBuf = new byte[65504];
			DatagramPacket inPkt = new DatagramPacket(inBuf, inBuf.length);
			s.receive(inPkt);

			byte[] temp = new byte[65504];
			temp = inPkt.getData();

			// Checks that the packet_number is in sequence
			if ((int)temp[0] == packet_number) {

				// Checks that packet is not the last packet
				if ((int)temp[1] != 1) {
					myFile.write(temp, 4, 65500);

					// Sends ACK of the next packet number if the packet received is in correct order
					String ACK = Integer.toString(packet_number+1);
					byte[] outACKbyte = new byte[1000];
					outACKbyte = ACK.getBytes();
					DatagramPacket outACK = new DatagramPacket(outACKbyte, outACKbyte.length, inPkt.getAddress(), inPkt.getPort());
					s.send(outACK);
					packet_number++;
				}
				
				// If the packet is the last packet, the last packet is written to file and the file is closed
				else {

					String ACK = Integer.toString(packet_number+1);
					byte[] outACKbyte = new byte[1000];
					outACKbyte = ACK.getBytes();
					DatagramPacket outACK = new DatagramPacket(outACKbyte, outACKbyte.length, inPkt.getAddress(), inPkt.getPort());

					s.send(outACK);

					int last_packet_length = (((((int)temp[2])&0xff) << 8) | (int)temp[3]&0xff);

					myFile.write(temp, 4, last_packet_length);
					myFile.close();

					// Creates reply to send to client, informing that the file is received
					String reply2 = "File received!";
					System.out.println(reply2);

					byte[] outBuf = new byte[1000];
					outBuf = reply2.getBytes();

					// Creates reply packet using output buffer.
					// Note: dest address/port is retrieved from inPkt
					DatagramPacket outPacket = new DatagramPacket(outBuf, outBuf.length, inPkt.getAddress(), inPkt.getPort());

					// Finally, send the packet
					s.send(outPacket);
				}
			}

			// If the packet_number is not in sequence, request for retransmission
			else {
				//Sends ACK for the previous packet number
				String ACK = Integer.toString(packet_number);
				byte[] outACKbyte = new byte[1000];
				outACKbyte = ACK.getBytes();
				DatagramPacket outACK = new DatagramPacket(outACKbyte, outACKbyte.length, inPkt.getAddress(), inPkt.getPort());

				s.send(outACK);
			}
		}
	}
}