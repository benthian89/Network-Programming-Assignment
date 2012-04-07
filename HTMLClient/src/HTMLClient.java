
import java.util.*;
import java.io.*;
import java.net.*;

import javax.swing.JFrame;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
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

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;


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
					
					JOptionPane.showMessageDialog(new JFrame(), "File received successfully!", "Grabbed", JOptionPane.PLAIN_MESSAGE );
			}
		});
		GetButton.setFont(new Font("Cooper Std Black", Font.PLAIN, 18));
		GetButton.setBounds(241, 165, 102, 38);
		getContentPane().add(GetButton);
		
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
	}
	
	@SuppressWarnings("deprecation")
	public static void HTMLGrabber() {
		
		String userInputAddress = websiteAddress;
		String host;
		String serverPathAddress;	
		String localFilePath = choosenFilePath.getPath();

		Socket s;
		FileOutputStream myFile;
		
		//Removes http:// if it starts with it
		if (userInputAddress.startsWith("http://")) {
			userInputAddress = userInputAddress.substring(7);
		}
		
		//if the website is http://www.free-extras.com/funny/images/392/silly+cat+chillin+in+a+cardboard+box.html
		//This will remove the host and left with /funny/images/392/silly+cat+chillin+in+a+cardboard+box.html
		if (userInputAddress.contains("/")) {
			int indexOfPath = userInputAddress.indexOf("/");		
			serverPathAddress = userInputAddress.substring(indexOfPath);
		
			//host will be www.free-extras.com in the example above
			host = userInputAddress.substring(0,indexOfPath);
		}
		
		// if website is www.gsmarena.com
		// serverPathAddress is at the root "/"
		else {
			serverPathAddress = "/";
			host = userInputAddress;
		}
				
		try {
					
			s = new Socket(host,80);
		
			//Create a output stream writer to "talk" to the webserver
			OutputStreamWriter outFromClient = new OutputStreamWriter(s.getOutputStream()); 
			
			outFromClient.write("GET " + serverPathAddress + " HTTP/1.0\r\n");
			outFromClient.write("Host: "+ host + "\r\n");
			outFromClient.write("\r\n");
			outFromClient.flush();
			
			//Creates a buffer to read in the response from the web server
			BufferedReader inFromServer = new BufferedReader (new InputStreamReader(s.getInputStream()));
			
			String input = null;
			String response = null;
			
			do { input = inFromServer.readLine(); 
				response = response + input + "\n"; 
			} 
			while (input != null);
			
			//response will display the response from the web server, including header + body
			response = response.substring(4);
		
			// Use html parser to get the image tags and fire GET and store the image
			Document doc = Jsoup.parse(response);
			
			//Finds all the img tags
			Elements images = doc.select("img[src]");
			int number_of_images = images.size();
			Vector<String> imagesList =  new Vector<String>(number_of_images);
		
			//Loops through all the img tags and adds them to an array.
			for (Element src : images) {			
					imagesList.add(src.attr("abs:src"));										
			}
			
			
			// Here, we will send a request for every img file and store the image to disk
			for (int i=0; i<number_of_images; i++) {
				String imageLink = imagesList.get(i);
				String hostName;
				String imagePathAddress;		
				String imageFileName;
				byte[] buf = new byte[10*1024*1024]; //10mb for buffer
				
				Socket imageSocket;				
				
				//Removes http:// if it starts with it
				if (imageLink.startsWith("http://")) {
					imageLink = imageLink.substring(7);
				}
				
						
					int indexOfImagePath = imageLink.indexOf("/");				
					imagePathAddress = imageLink.substring(indexOfImagePath);
					
					hostName = imageLink.substring(0,indexOfImagePath);
				
					//Gets the image file name to be used as the saved file name
					int lastSlash = imageLink.lastIndexOf("/");
					imageFileName = imageLink.substring(lastSlash+1);
					
				
				try {
					imageSocket = new Socket(hostName,80);
					
					DataInputStream responseFromServer = new DataInputStream ((imageSocket.getInputStream()));
									
					OutputStreamWriter requestForImages = new OutputStreamWriter(imageSocket.getOutputStream()); 
					requestForImages.write("GET " + imagePathAddress + " HTTP/1.0 \r\n");
					requestForImages.write("Host: "+ hostName + "\r\n");
					requestForImages.write("\r\n");
					requestForImages.flush();
					
					
					//Stores the image to the folder chosen by the user
					myFile = new FileOutputStream(localFilePath + "/" + imageFileName);
					
					
					while (!responseFromServer.readLine().equals("")) {
						//ignore the header
					}
					
					int length=0;
					//Reads in the data (in binary) and write to file
					while ((length = responseFromServer.read(buf))>0 ) {
						myFile.write(buf,0,length);
					}
									
					myFile.close();
				} catch (IOException e) {
					System.out.println("Error getting page " + e);
				}
			}
			
			s.close();
		} catch (IOException e) {
			System.out.println("Error getting page " + e);
		}
	}
	
	
	
    public static void main (String args[]) throws Exception 
	{
		
		HTMLClient myHTML = new HTMLClient();
		myHTML.setSize(600, 330);
        myHTML.setMinimumSize(new Dimension(600, 330));
    	myHTML.setVisible(true);
    	Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
		int X = (screen.width / 2) - 400; // Center horizontally.
		int Y = (screen.height / 2) - 300; // Center vertically.
		myHTML.setBounds(X, Y, 600, 330);		

	}
}
