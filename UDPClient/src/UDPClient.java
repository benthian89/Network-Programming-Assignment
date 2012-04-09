//CS2105 Programming Assignment Part A
//Group 36
//A0072967N Koh Zhi Kai
//A0073002B Thian Chang Yi Benjamin

import java.net.*;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Toolkit;
import java.io.*;
import javax.swing.*;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

@SuppressWarnings("serial")
public class UDPClient extends JFrame {
  private static JTextArea filePath;
  private static File chosenFile;
  private static String filename;

  	public UDPClient() {
	    getContentPane().setLayout(null);
	    setTitle("UDP Client");
	    try {
	      UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
	    } catch (Exception e) {
	      System.out.println("Unable to load Windows look and feel.");
	    }
	
	    JLabel lblFilePath = new JLabel("<html><p>Choose file</p> to send</html>:");
	    lblFilePath.setFont(new Font("Gill Sans MT", 1, 14));
	    lblFilePath.setBounds(10, 95, 73, 48);
	    getContentPane().add(lblFilePath);
	
	    JScrollPane scrollPane = new JScrollPane();
	    scrollPane.setVerticalScrollBarPolicy(21);
	    scrollPane.setBounds(90, 98, 384, 45);
	    getContentPane().add(scrollPane);
	
	    filePath = new JTextArea();
	    filePath.setFont(new Font("Tahoma", 0, 13));
	    scrollPane.setViewportView(filePath);
	    filePath.setEditable(false);
	
	    JButton btnBrowse = new JButton("browse");
	    btnBrowse.setFont(new Font("Tahoma", 1, 12));
	    btnBrowse.addActionListener(new ActionListener() {
	      public void actionPerformed(ActionEvent arg0) {
	        JFileChooser chooser = new JFileChooser(".");
	        chooser.showOpenDialog(null);
	        UDPClient.chosenFile = chooser.getSelectedFile();
	        UDPClient.filename = UDPClient.chosenFile.getName();
	        UDPClient.filePath.setText(UDPClient.chosenFile.getPath());
	      }   	
	    });
	    btnBrowse.setBounds(484, 104, 89, 32);
	    getContentPane().add(btnBrowse);
	
	    JButton SendButton = new JButton("Send");
	    SendButton.addActionListener(new ActionListener() {
	      public void actionPerformed(ActionEvent e) {
	        try {
	          UDPClient.this.transferFileName();
	        } catch (java.net.UnknownHostException e1) {
	          JOptionPane.showMessageDialog(new JFrame(), "Unable to send file!", "Error", JOptionPane.ERROR_MESSAGE);
	        } catch (IOException e1) {
	          JOptionPane.showMessageDialog(new JFrame(), "Unable to send file!", "Error", JOptionPane.ERROR_MESSAGE);
	        }
	      }
	    });
	    SendButton.setFont(new Font("Cooper Std Black", 0, 18));
	    SendButton.setBounds(241, 165, 102, 38);
	    getContentPane().add(SendButton);
	
	    JLabel lblNewLabel = new JLabel("UDP Client");
	    lblNewLabel.setFont(new Font("Tahoma", 2, 22));
	    lblNewLabel.setBounds(10, 11, 184, 38);
	    getContentPane().add(lblNewLabel);
	
	    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
  }

  public void transferFileName() throws IOException {
		//Use DataGramSocket for UDP connection
		DatagramSocket s = new DatagramSocket();
		
		//Addr and port number created
	    InetAddress addr = InetAddress.getByName("localhost");
	    int port = 9001;
	
	    //Converts filename to byte for sending via DatagramPacket
	    byte[] filenameByte = filename.getBytes();
	    
	    //Created a packet ( with destination addr and port )
		//Sends over the filename first
	    DatagramPacket outFilename = new DatagramPacket(filenameByte, filenameByte.length, addr, port);
	
	    s.send(outFilename);
	    
	    //Creates a packet buffer to store data from packets received.
	    byte[] infilenameReply = new byte[1000];
	    DatagramPacket infilename = new DatagramPacket(infilenameReply, infilenameReply.length);
	    
	    //Receives the reply from server.
	    s.receive(infilename);
	
	    String reply = new String(infilename.getData(), 0, infilename.getLength());
	    
	    //Checks if the server has received the filename 
	    if (reply.equalsIgnoreCase("Filename received.")) {
	      transferFile();
	    }
	    else {
	      JOptionPane.showMessageDialog(new JFrame(), "Server did not get the file's name", "Error", JOptionPane.ERROR_MESSAGE);
	      System.exit(0);
	    }
  }

  public void transferFile() throws IOException {
	    DatagramSocket s = new DatagramSocket();
	
	    InetAddress addr = InetAddress.getByName("localhost");
	    int port = 9001;
	    
	    //Convert the file into byteArray
	    FileInputStream fis = new FileInputStream(chosenFile);
	
	    //Checks if the file is too large. Cannot send more than 2GB due to the limit of Integer.
	    long length = chosenFile.length();
	    if (length > Integer.MAX_VALUE) {
	      JOptionPane.showMessageDialog(new JFrame(), "File is too big!\nMaximum 2GB file size", "Error", 0);
	      System.exit(0);
	    }
		
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
	    
	    if ((int)length % 65500 == 0) {
	      packets_remaining = (int)length / 65500;
	    }
	    else {
	      packets_remaining = (int)length / 65500 + 1;
	    }
	 
	    // Flag for indication of last packet
	    int last_packet = -1;
	    
	    // 1 byte is used for the last packet flag
	    byte[] previousPacket = new byte[65504];
	    int packet_number = 0;
	    boolean canSend = true;
	    ACKTimer t = new ACKTimer();
	    int timeout_counter = 0;
	
	    while (packets_remaining > 0) {
	      if (canSend) {
	        byte[] outBuf = new byte[65504];
	        packet_number++;
	        
	        /**** packet numbering will start from 1 ****/
	        
	        outBuf[0] = (byte)packet_number;
	        outBuf[2] = (byte)0;
	
	        // Checks if the packet is the last packet
	        if (packets_remaining == 1) {
	          last_packet = 1;
	          byte last_packetByte = (byte)last_packet;
	          outBuf[1] = last_packetByte;
	          outBuf[2] = (byte)(((int)length % 65500 & 65280) >> 8);
	          outBuf[3] = (byte)((int)length % 65500 & 255);
	          fis.read(outBuf, 4, (int)length % 65500);
	        }
	        else {
	          last_packet = 0;
	          byte last_packetByte = (byte)last_packet;
	          outBuf[1] = last_packetByte;
	          outBuf[2] = (byte) 65500;
	          fis.read(outBuf, 4, 65500);
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
	      byte[] inACKbyte = new byte[1000];
	      DatagramPacket inACK = new DatagramPacket(inACKbyte, inACKbyte.length);
	      s.receive(inACK);
	
	      String ACK = new String(inACK.getData(), 0, inACK.getLength());
	      int integer_ACK = Integer.parseInt(ACK);
	      
	      //ACK received and ACK number is the next packet to send
	      if (integer_ACK == packet_number + 1) {
	        t.StopTimer();
	        canSend = true;
	        packets_remaining--;
	        timeout_counter = 0;
	      }
	      
	      // If ACK is the previous packet number or time out occurs, retransmit the packet
	      if ((integer_ACK == packet_number) || (t.isTimeOut())) {
	        DatagramPacket outPkt = new DatagramPacket(previousPacket, previousPacket.length, addr, port);
	        s.send(outPkt);
	        t = new ACKTimer(2); // 2 sec timeout
	        canSend = false;
	        timeout_counter++;
	      }
	      
	      // If time out occurs 3 times, the client will stop transmission.
	      if (timeout_counter > 3) {
	        JOptionPane.showMessageDialog(new JFrame(), "Time out has occurred 3 times\ntransmission will terminate now!", "Error", JOptionPane.ERROR_MESSAGE);
	        System.exit(0);
	      }
	
	    }
	
	    // Creates a packet buffer to store final reply from server that the file is received
	    byte[] inBuf = new byte[1000];
	    DatagramPacket inPkt = new DatagramPacket(inBuf, inBuf.length);
	
	    // receive the reply from server.
	    s.receive(inPkt);
	
	    //Checks if the server has received the file
	    String reply2 = new String(inPkt.getData(), 0, inPkt.getLength());
	    if (reply2.equalsIgnoreCase("File received!")) {
	    	JOptionPane.showMessageDialog(new JFrame(), "Server: " + reply2 + "\n" + "File sent successfully!", "Successful", JOptionPane.PLAIN_MESSAGE);
	    }
	    else {
	    	JOptionPane.showMessageDialog(new JFrame(), "Error sending file!", "Error", JOptionPane.ERROR_MESSAGE);
	    	System.exit(0);
	    }
	   
  }

  public static void main(String[] args)
    throws Exception
  {
    UDPClient myUDP = new UDPClient();
    myUDP.setSize(600, 330);
    myUDP.setMinimumSize(new Dimension(600, 330));
    myUDP.setVisible(true);
    Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
    int X = screen.width / 2 - 400;
    int Y = screen.height / 2 - 300;
    myUDP.setBounds(X, Y, 600, 330);
  }
}