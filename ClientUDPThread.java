/*
 * Filename: ClientUDPThread.java
 * Description: An echo client using connectionless delivery system (UDP).
 *              Sends character messages to a server which are echoed capitalized.
 *              No error handling and exceptions are implemented.
 * Operation: java UDPEchoCLient [hostname] [port]
 *
 */

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.*;
import java.io.*;

public class ClientUDPThread extends JFrame implements Runnable{
	private DatagramSocket datagramSocket;
	private InetAddress serverAddr;
	private int port;
	private String name;
	Thread t;
	private long startTime;
	private long timeAfterToClose;

	private JFrame jframe;

	private JPanel usersAndChatroomsJpanel;
	private JTextArea users;

	private JPanel inputAndSendJpanel;
	private JTextField userText;
	private JTextArea chatWindow;
	private JButton sendButton;

	private JPanel kickUserArea;
	private JButton kickButton;

	public synchronized long getStart()
	{
		return startTime;
	}

	public synchronized void setStart(long startT){
		startTime = startT;
	}




	//update chat window
	private void showMessage(final String message){
		SwingUtilities.invokeLater(
				new Runnable(){
					public void run(){
						chatWindow.append("\n"+message);
					}
				}
		);
	}

	//update users window
	private void showUsers(final String message){
		SwingUtilities.invokeLater(
				new Runnable(){
					public void run(){
						users.setText(message);
					}
				}
		);
	}

	//allows user to type
	private void ableToType(final boolean tof){
		SwingUtilities.invokeLater(
				new Runnable(){
					public void run(){
						userText.setEditable(tof);
					}
				}
		);
	}
	private void sendMessage(String message){
		byte[] data = new byte[255];

		/* Convert the string message into bytes */
		data = message.getBytes();

		/* Create datagram to send to server specifying message, message length, server address, port */
		DatagramPacket outToServer = new DatagramPacket( data, data.length, serverAddr, port );

		/* Send the datagram through the datagramSocket */
		try {
			datagramSocket.send( outToServer );
			setStart(System.currentTimeMillis());
			//startTime = System.currentTimeMillis();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}



	public void initGUI(String chatName){
		jframe = new JFrame(chatName);


		//KICK USER AREA
		kickUserArea = new JPanel();
		kickUserArea.setLayout(new FlowLayout());
		kickButton = new JButton("Kick user");
		kickButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Object[] options1 = { "Kick",
						"Quit" };
				JPanel panel = new JPanel();
				panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
				panel.add(new JLabel("Enter user's name to kick:"));
				JTextField textFieldName = new JTextField(10);
				panel.add(textFieldName);

				int result = JOptionPane.showOptionDialog(jframe, panel, "Kick user!!!",
						JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE,
						null, options1, null);
				if (result == JOptionPane.YES_OPTION) {

					String username;
					if (textFieldName.getText().equals("")){
						username = "qsdklfsdlfksdjl";
					}
					else{
						username = textFieldName.getText();
					}
					sendMessage("@@00kick00"+username+"00kick00@@");
				}
			}
		});
		kickUserArea.add(kickButton);


		//INPUT AND SEND BUTTON
		inputAndSendJpanel = new JPanel();
		inputAndSendJpanel.setLayout(new FlowLayout());

		//USERS  AREA
		usersAndChatroomsJpanel = new JPanel();
		usersAndChatroomsJpanel.setLayout(new BoxLayout(usersAndChatroomsJpanel, BoxLayout.Y_AXIS));
		users = new JTextArea();
		users.setFont(users.getFont().deriveFont(12f));
		users.setEditable(false);
		users.setBorder(new EmptyBorder(20, 20, 20, 20));
		users.setBackground(Color.GRAY);
		usersAndChatroomsJpanel.add(users);



		userText = new JTextField("", 20);
		userText.setFont(userText.getFont().deriveFont(12f));
		userText.setEditable(false);
		userText.addActionListener(
				new ActionListener(){
					public void actionPerformed(ActionEvent event){
						sendMessage(event.getActionCommand());
						userText.setText("");
					}
				}
		);
		sendButton = new JButton("SEND");
		sendButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				sendMessage(userText.getText());
				userText.setText("");
			}
		});
		inputAndSendJpanel.add(userText);
		inputAndSendJpanel.add(sendButton);
		chatWindow = new JTextArea();
		chatWindow.setFont(chatWindow.getFont().deriveFont(12f));
		chatWindow.setEditable(false);
		jframe.add(kickUserArea,BorderLayout.NORTH);
		jframe.add(new JScrollPane(chatWindow),BorderLayout.CENTER);
		jframe.add(usersAndChatroomsJpanel,BorderLayout.EAST);
		jframe.add(inputAndSendJpanel,BorderLayout.SOUTH);
		jframe.addWindowListener(new java.awt.event.WindowAdapter() {
			@Override
			public void windowClosed(java.awt.event.WindowEvent windowEvent) {
				sendMessage("@@exitFROMchat@@");
			}
		});
		jframe.setSize(500, 450);
		jframe.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		jframe.setLocationByPlatform(true);
		jframe.setVisible(true);
	}

	public ClientUDPThread(int port, String serverIPAddress, String name, String chatName, int timeAfterToClose) throws IOException {
		this.name = name;
		startTime = System.currentTimeMillis();
		this.timeAfterToClose = timeAfterToClose;

		SwingUtilities.invokeLater( new Runnable() {
			public void run() {
				try {
					UIManager.setLookAndFeel(
							UIManager.getSystemLookAndFeelClassName());
				} catch (Exception useDefault) {}
				initGUI(chatName);
			}
		});


		this.port = port;
		String hostname;
		
		/* First argument is the running server's name */
		hostname = new String( serverIPAddress );

		/* Determine the IP address of the server from the hostname */
		serverAddr = InetAddress.getByName(hostname);

		/* Create a datagram datagramSocket through which the data will be send */
		datagramSocket = new DatagramSocket();
		//This starts the thread for input(message from client to server)
		t = new Thread(this);
		t.start();
		ableToType(true);
		//This create and starts thread for output(server to client)
		Thread threadOutput = new Thread(new Runnable() { public void run() {
			String lineFromServer;
			while ( true ) {
				/* Create array of 255 raw bytes to hold incomingmessage */
				byte [] response = new byte[255];
				/* Create a datagram to receive from server specifying the message received */
				DatagramPacket inFromServer = new DatagramPacket( response, 255);

				/* Receive the echo datagram from server (capitalized) */
				try {
					datagramSocket.receive( inFromServer );
				} catch (IOException e) {
					e.printStackTrace();
				}
				/* Convert received byte array to string for displaying */
				lineFromServer = new String( inFromServer.getData(), 0, inFromServer.getLength());
				/* Output echoed message to the screen */
				System.out.println( "Received: " + lineFromServer );
				String[] arrOfStr = lineFromServer.split("@@");
				if (arrOfStr.length >1) {
					String specialMsg = arrOfStr[1];
					if(specialMsg.split("00users00").length > 1){
						String[] stringContainsUsers = specialMsg.split("00users00");
						String[] users = stringContainsUsers[1].split("##");
						String usersToShow = "USERS";
						for (int i=0;i<users.length;i++){
							if (users[i].equals(name)){
								usersToShow+= "\n"+users[i]+"(ME)";
							}
							else{
								usersToShow+= "\n"+users[i];
							}

						}
						showUsers(usersToShow);
						continue;
					}
					else if (specialMsg.split("00kickedOUT00").length > 1){
						jframe.dispose();
					}
				}
				showMessage(lineFromServer );
			}
		}});
		threadOutput.start();

		//Thread checks if user has not typed and kicks him

		if (timeAfterToClose>0){
			Thread threadTimerToKickOut = new Thread(new Runnable() { public void run() {
				while((System.currentTimeMillis()-getStart())<timeAfterToClose)
				{
				}
				showMessage("You have not been active for long enough byeee!!");
				try {
					Thread.sleep(5000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				jframe.dispose();
			}});
			threadTimerToKickOut.start();
		}

	}



	@Override
	public void run() {
		String lineToServer = null;
		sendMessage("@@00init00"+name+"00init00@@");
		while(true){
			System.out.println( "Type text to send to server: " );

			/* Create a buffer to hold the user's input */
			BufferedReader userInput = new BufferedReader( new InputStreamReader( System.in ) );

			/* Get the user's input */
			try {
				lineToServer = userInput.readLine();
			} catch (IOException e) {
				e.printStackTrace();
			}

			/* Stop infinite loop if user wants to stop getting echos by typing exit */
			if ( lineToServer.equals( "exit" ) )
				break;

			sendMessage(lineToServer);
		}
	}
}

