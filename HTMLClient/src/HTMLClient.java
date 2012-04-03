
import java.util.*;
import java.io.*;
import java.net.*;

import javax.swing.JFrame;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import javax.swing.JTextField;
import javax.swing.JLayeredPane;
import javax.swing.JLabel;
import java.awt.Font;
import javax.swing.JButton;
import javax.swing.JTextPane;
import javax.swing.JTextArea;


public class HTMLClient extends JFrame{
	private static JTextArea Url;
	private static JTextArea filePath;
	
	public HTMLClient() {
		getContentPane().setLayout(null);
		
		
		JLabel lblNewLabel = new JLabel("URL:");
		lblNewLabel.setFont(new Font("Gill Sans MT", Font.BOLD, 14));
		lblNewLabel.setBounds(10, 33, 41, 29);
		getContentPane().add(lblNewLabel);
		
		JLabel lblFilePath = new JLabel("File Path:");
		lblFilePath.setFont(new Font("Gill Sans MT", Font.BOLD, 14));
		lblFilePath.setBounds(10, 95, 68, 29);
		getContentPane().add(lblFilePath);
		
		JButton btnNewButton = new JButton("Get");
		btnNewButton.setFont(new Font("Cooper Std Black", Font.PLAIN, 18));
		btnNewButton.setBounds(241, 165, 102, 38);
		getContentPane().add(btnNewButton);
			
		Url = new JTextArea();
		Url.setWrapStyleWord(true);
		Url.setLineWrap(true);
		Url.setBounds(90, 36, 484, 22);
		getContentPane().add(Url);		
		
		filePath = new JTextArea();
		filePath.setBounds(90, 98, 484, 22);
		getContentPane().add(filePath);
	}
	
    public static void main (String args[]) throws Exception 
	{
		// throws Exception here because don't want to deal
		// with errors in the rest of the code for simplicity.
		
		Socket s;
		FileOutputStream myFile;
		String websiteAddress;
		String HTMLFile;
		String localFile = filePath.getText();
		
		try {
			websiteAddress = Url.getText();
			
			s = new Socket(websiteAddress,80);
			
			BufferedReader inFromServer = new BufferedReader (new InputStreamReader(s.getInputStream()));
			
			OutputStreamWriter outFromClient = new OutputStreamWriter(s.getOutputStream()); 
			
			outFromClient.write("GET " + websiteAddress + " HTTP/1.0\r\n");
			outFromClient.flush();
			
			localFile = localFile.substring(localFile.lastIndexOf('/') +1);
			
			BufferedWriter writeToLocalFile = new BufferedWriter(new FileWriter(localFile));
			
			boolean flag = true;
			String input;
			
			while (flag) {
				input = inFromServer.readLine();
				
				if (input == null)
					flag = false;
				else {
					writeToLocalFile.write(input);
				}
			}
			
			System.out.println("\nPage received successfully...\n" );
			
			writeToLocalFile.close();
			//myFile = new FileOutputStream("Data/"+filename);
			
			s.close();
		} catch (IOException e) {
			System.out.println("Error getting page " + e);
		}
		
		
		
		
		
		
		
		
	/*	
		
		while (true) 
		{
			//Listen & Accept Connection and Create new CONNECTION SOCKET
			Socket s = serverSock.accept();
			System.out.println("connection established from " + s.getInetAddress());
			System.out.println("COnnection Definition " + s.toString());

			
			// The next 3 lines create a buffer reader that
			// reads from the socket s.
			InputStream is = s.getInputStream();
			InputStreamReader isr = new InputStreamReader(is);
			BufferedReader br = new BufferedReader(isr);

			// The next 2 lines create a output stream we can
			// write to.
			OutputStream os = s.getOutputStream();
			DataOutputStream dos = new DataOutputStream(os);
			
			// Read HTTP request (empty line signal end of request)
			String input = br.readLine();
			String filename = "";
			StringTokenizer st = new StringTokenizer(input);
		
			if (st.nextToken().equals("GET"))
			{
				// This is a GET request.  Parse filename.
				filename = st.nextToken();
				if (filename.startsWith("/")) 
				{
					filename = filename.substring(1);
					
			    }
			    //filename = "root/" + filename; //my web folder
			}
			// read and throw away the rest of the HTTP request
			while (input.compareTo("") != 0) 
			{
				input = br.readLine();  //Just read and ignore
			}
			
			try{
			
			// Open and read the file into buffer
			File f = new File(filename);
		      
			if (f.canRead())
			{
				int size = (int)f.length();
				
				//Create a File InputStrem to read the File
				FileInputStream fis = new FileInputStream(filename);
				byte[] buffer = new byte[size];
				fis.read(buffer);
			
				// Now, write buffer to client
				// (but, send HTTP response header first)
				
				dos.writeBytes("HTTP/1.0 200 Okie \r\n");
				dos.writeBytes("Content-type: text/html\r\n");
				dos.writeBytes("\r\n");
				dos.write(buffer,  0, size);
			}
			else
			{
				// File cannot be read.  Reply with 404 error.
				dos.writeBytes("HTTP/1.0 404 Not Found\r\n");
				dos.writeBytes("\r\n");
				dos.writeBytes("Cannot find " + filename + " leh");
			}
			}
			catch (Exception ex){
			}

			// Close connection (using HTTP 1.0 which is non-persistent).
			s.close();
		}
		*/
	}
}
