
import java.util.*;
import java.io.*;
import java.net.*;

import javax.swing.JFrame;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JFileChooser;
import javax.swing.JTextField;
import javax.swing.JLayeredPane;
import javax.swing.JLabel;
import javax.swing.UIManager;

import java.awt.Font;
import javax.swing.JButton;
import javax.swing.JTextPane;
import javax.swing.JTextArea;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;


public class HTMLClient extends JFrame{
	private static JTextArea Url;
	private static JTextArea filePath;
	private static String websiteAddress;
	private static File choosenFilePath;
	
	public HTMLClient() {
		getContentPane().setLayout(null);
		
		try {
			UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
		} catch (Exception e) {
			System.out.println("Unable to load Windows look and feel.");
		}
		
		JLabel lblNewLabel = new JLabel("URL:");
		lblNewLabel.setFont(new Font("Gill Sans MT", Font.BOLD, 14));
		lblNewLabel.setBounds(10, 33, 41, 29);
		getContentPane().add(lblNewLabel);
			
		Url = new JTextArea();
		Url.setFont(new Font("Tahoma", Font.PLAIN, 13));
		Url.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent e) {			
					websiteAddress = Url.getText();				
			}
		});
		Url.setWrapStyleWord(true);
		Url.setLineWrap(true);
		Url.setBounds(90, 36, 484, 22);
		getContentPane().add(Url);		
		
		JLabel lblFilePath = new JLabel("File Path:");
		lblFilePath.setFont(new Font("Gill Sans MT", Font.BOLD, 14));
		lblFilePath.setBounds(10, 95, 68, 29);
		getContentPane().add(lblFilePath);
		
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
		scrollPane.setBounds(90, 98, 384, 45);
		getContentPane().add(scrollPane);
		
		filePath = new JTextArea();
		filePath.setFont(new Font("Tahoma", Font.PLAIN, 13));
		scrollPane.setViewportView(filePath);
		filePath.setEditable(false);
		
		JButton btnBrowse = new JButton("browse");
		btnBrowse.setFont(new Font("Tahoma", Font.BOLD, 12));
		btnBrowse.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				JFileChooser chooser = new JFileChooser(".");
				chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				chooser.showOpenDialog(null);	
				choosenFilePath = chooser.getSelectedFile();
				filePath.setText(choosenFilePath.getPath());
			}
		});
		btnBrowse.setBounds(485, 99, 89, 23);
		getContentPane().add(btnBrowse);
		
		JButton GetButton = new JButton("Get");
		GetButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {			
					HTMLGrabber();				
			}
		});
		GetButton.setFont(new Font("Cooper Std Black", Font.PLAIN, 18));
		GetButton.setBounds(241, 165, 102, 38);
		getContentPane().add(GetButton);
	}
	
	public static void HTMLGrabber() {
		
		String userInputAddress = websiteAddress;
		String host;
		String serverPathAddress;
		String htmlFileName;
		String localFile = null;
		String localFilePath = choosenFilePath.getPath();

		Socket s;
		FileOutputStream myFile;
		
		//Removes http:// if it starts with it
		if (userInputAddress.startsWith("http://")) {
			userInputAddress = userInputAddress.substring(7);
		}
		
		int indexOfPath = userInputAddress.indexOf("/");
		
		serverPathAddress = userInputAddress.substring(indexOfPath);
		
		host = userInputAddress.substring(0,indexOfPath);
		
		//Gets the html file name to be used as the saved file name
		int lastIndex = userInputAddress.lastIndexOf("/");
		htmlFileName = userInputAddress.substring(lastIndex);
		localFile = htmlFileName;
		
		//Set default file name to index.html
		if (htmlFileName.length() == 0) {
			htmlFileName = serverPathAddress;
			localFile = "index.html";
		}
		
		try {
					
			s = new Socket(host,80);
			
			BufferedReader inFromServer = new BufferedReader (new InputStreamReader(s.getInputStream()));
			
			//Create a output stream writer to "talk" to the webserver
			OutputStreamWriter outFromClient = new OutputStreamWriter(s.getOutputStream()); 
			System.out.println("GET " + serverPathAddress + " HTTP/1.0 \n");
			System.out.println("Host: "+ host);
			System.out.println("");
			
			outFromClient.write("GET " + serverPathAddress + " HTTP/1.0 \n");
			outFromClient.write("Host: "+ host);
			outFromClient.write("");
			outFromClient.flush();
			
			localFile = localFile.substring(localFile.lastIndexOf('/') +1);
			
			//creating a BufferWriter to create and write into the file locally	
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
	}
	
	
	
    public static void main (String args[]) throws Exception 
	{
		// throws Exception here because don't want to deal
		// with errors in the rest of the code for simplicity.
		HTMLClient myHTML = new HTMLClient();
		myHTML.setSize(600, 330);
        myHTML.setMinimumSize(new Dimension(600, 330));
    	myHTML.setVisible(true);
		
		
		
		
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
