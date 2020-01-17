/*
 * Filename: TCPEchoClient.java
 * Description: An echo client using connection-oriented delivery system (TCP).
 *              Sends character messages to a server which are echoed capitalized.
 *              Error handling and exceptions are implemented!
 * Operation: java TCPEchoClient [hostname] [port]
 *
 */

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.*;


public class ClientTCPThread extends JFrame implements Runnable{
	private String nameOfClient;
	PrintStream outToServer;
	/* For reading from user */
	BufferedReader inFromUser;
	/* Hold user input */
	String sentence = null;

	Thread t;

	private JFrame jframe;


	private JPanel manageChatroomButtonsArea;
	private JButton createChatroom;
	private JButton connectChatroom;
	private JButton deleteChatroom;
	private JButton changeOwnerChatroom;
	private JButton privateMessage;

	private JPanel usersAndChatroomsJpanel;
	private JTextArea users;
	private JTextArea chatrooms;

	private JPanel inputAndSendJpanel;
	private JTextField userText;
	private JTextArea chatWindow;
	private JButton sendButton;






	public void initGUI(){

		jframe = new JFrame("Your SERVER window");

		//Manage chatroom button area
		manageChatroomButtonsArea = new JPanel();
		manageChatroomButtonsArea.setLayout(new FlowLayout());
		createChatroom = new JButton("Create ChatRoom");
		createChatroom.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Object[] options1 = { "Create",
						"Quit" };
				JPanel panel = new JPanel();
				panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
				panel.add(new JLabel("Enter chatroom name:"));
				JTextField textFieldName = new JTextField(10);
				panel.add(textFieldName);
				panel.add(new JLabel("Enter chatroom password(let blank if no code needed):"));
				JTextField textFieldPass = new JTextField(10);
				panel.add(textFieldPass);
				panel.add(new JLabel("Enter time limit before users gets kicked for no typing(let empty if not needed):"));
				JTextField textFieldTime = new JTextField(10);
				panel.add(textFieldTime);
				JCheckBox invitation = new JCheckBox("Connect with invitation");
				panel.add(invitation);

				int result = JOptionPane.showOptionDialog(jframe, panel, "Create a new chat room!!!",
						JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE,
						null, options1, null);
				if (result == JOptionPane.YES_OPTION) {

					String chatname = textFieldName.getText();
					String chatpass;
					String time;
					if (textFieldPass.getText().equals("")){
						chatpass = "empty";
					}
					else{
						chatpass = textFieldPass.getText();
					}

					if (textFieldTime.getText().equals("")){
						time = "0";
					}
					else{
						time = textFieldTime.getText();
					}
					if (invitation.isSelected()){
						sendMessage("@@00create00"+chatname+"##empty##"+time+"##true"+"00create00@@",outToServer);
					}
					else{
						sendMessage("@@00create00"+chatname+"##"+chatpass+"##"+time+"##false"+"00create00@@",outToServer);
					}

				}
			}
		});

		connectChatroom = new JButton("Connect to ChatRoom");
		connectChatroom.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Object[] options1 = { "Connect",
						"Quit" };
				JPanel panel = new JPanel();
				panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
				panel.add(new JLabel("Enter chatroom name:"));
				JTextField textFieldName = new JTextField(10);
				panel.add(textFieldName);
				panel.add(new JLabel("Enter chatroom password(let blank if no code needed):"));
				JTextField textFieldPass = new JTextField(10);
				panel.add(textFieldPass);

				int result = JOptionPane.showOptionDialog(jframe, panel, "Connect to chat room!!!",
						JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE,
						null, options1, null);
				if (result == JOptionPane.YES_OPTION) {

					String chatname;
					if (textFieldName.getText().equals("")){
						chatname = "dkvdlvkdfvlfdkm";
					}
					else{
						chatname = textFieldName.getText();
					}
					String chatpass;
					if (textFieldPass.getText().equals("")){
						chatpass = "empty";
					}
					else{
						chatpass = textFieldPass.getText();
					}
					sendMessage("@@00connect00"+chatname+"##"+chatpass+"00connect00@@",outToServer);
				}

			}
		});

		deleteChatroom = new JButton("Delete ChatRoom");
		deleteChatroom.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Object[] options1 = { "Delete",
						"Quit" };
				JPanel panel = new JPanel();
				panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
				panel.add(new JLabel("Enter chatroom name to delete:"));
				JTextField textFieldName = new JTextField(10);
				panel.add(textFieldName);

				int result = JOptionPane.showOptionDialog(jframe, panel, "Delete chat room!!!",
						JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE,
						null, options1, null);
				if (result == JOptionPane.YES_OPTION) {

					String chatname;
					if (textFieldName.getText().equals("")){
						chatname = "qsdklfsdlfksdjl";
					}
					else{
						chatname = textFieldName.getText();
					}
					sendMessage("@@00delete00"+chatname+"00delete00@@",outToServer);
				}
			}
		});

		changeOwnerChatroom = new JButton("Change creator of ChatRoom");
		changeOwnerChatroom.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Object[] options1 = { "Change owner",
						"Quit" };
				JPanel panel = new JPanel();
				panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
				panel.add(new JLabel("Enter chatroom name:"));
				JTextField textFieldChatName = new JTextField(10);
				panel.add(textFieldChatName);
				panel.add(new JLabel("Enter the user name of new owner:"));
				JTextField textFieldUserName = new JTextField(10);
				panel.add(textFieldUserName);

				int result = JOptionPane.showOptionDialog(jframe, panel, "Change chat room owner!!!",
						JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE,
						null, options1, null);
				if (result == JOptionPane.YES_OPTION) {

					String chatname;
					String username;
					if (textFieldChatName.getText().equals("")){
						chatname = "dfkvjdflkvjfdlv";
					}
					else{
						chatname = textFieldChatName.getText();
					}

					if (textFieldUserName.getText().equals("")){
						username = "ogojfovrodcd";
					}
					else{
						username = textFieldUserName.getText();
					}
					sendMessage("@@00changeowner00"+username+"##"+chatname+"00changeowner00@@",outToServer);
				}
			}
		});


		privateMessage = new JButton("Send private message");
		privateMessage.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Object[] options1 = { "Send",
						"Quit" };
				JPanel panel = new JPanel();
				panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
				panel.add(new JLabel("Enter the user name:"));
				JTextField textFieldUserName = new JTextField(10);
				panel.add(textFieldUserName);

				int result = JOptionPane.showOptionDialog(jframe, panel, "Send private message!!!",
						JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE,
						null, options1, null);
				if (result == JOptionPane.YES_OPTION) {

					String username;
					if (textFieldUserName.getText().equals("")){
						username = "ogojfovrodcd";
					}
					else{
						username = textFieldUserName.getText();
					}
					sendMessage("@@00privateChat00"+nameOfClient+"##"+username+"00privateChat00@@",outToServer);
				}
			}
		});

		manageChatroomButtonsArea.add(createChatroom);
		manageChatroomButtonsArea.add(connectChatroom);
		manageChatroomButtonsArea.add(deleteChatroom);
		manageChatroomButtonsArea.add(changeOwnerChatroom);
		manageChatroomButtonsArea.add(privateMessage);


		//INPUT AND SEND BUTTON
		inputAndSendJpanel = new JPanel();
		inputAndSendJpanel.setLayout(new FlowLayout());

		//USERS AND CHAT ROOM AREA
		usersAndChatroomsJpanel = new JPanel();
		usersAndChatroomsJpanel.setLayout(new BoxLayout(usersAndChatroomsJpanel, BoxLayout.Y_AXIS));
		users = new JTextArea();
		users.setFont(users.getFont().deriveFont(12f));
		users.setEditable(false);
		users.setBorder(new EmptyBorder(20, 20, 20, 20));
		users.setBackground(Color.GRAY);
		usersAndChatroomsJpanel.add(users);


		chatrooms = new JTextArea();
		chatrooms.setFont(chatrooms.getFont().deriveFont(12f));
		chatrooms.setEditable(false);
		chatrooms.setBorder(new EmptyBorder(20, 20, 20, 20));
		chatrooms.setBackground(Color.YELLOW);
		usersAndChatroomsJpanel.add(chatrooms);


		////INPUT text area and send button
		userText = new JTextField("", 20);
		userText.setFont(userText.getFont().deriveFont(12f));
		userText.setEditable(false);
		userText.addActionListener(
				new ActionListener(){
					public void actionPerformed(ActionEvent event){
						sendMessage(event.getActionCommand(),outToServer);
						userText.setText("");
					}
				}
		);
		sendButton = new JButton("SEND");
		//When clicked sends message
		sendButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				sendMessage(userText.getText(),outToServer);
				userText.setText("");
			}
		});
		inputAndSendJpanel.add(userText);
		inputAndSendJpanel.add(sendButton);
		chatWindow = new JTextArea();
		chatWindow.setFont(chatWindow.getFont().deriveFont(12f));
		chatWindow.setEditable(false);
		//When X clicked it sends message to server to log out and ends client process
		jframe.addWindowListener(new java.awt.event.WindowAdapter() {
			@Override
			public void windowClosed(java.awt.event.WindowEvent windowEvent) {
				sendMessage("exit",outToServer);
				System.exit(0);
			}
		});

		jframe.add(manageChatroomButtonsArea,BorderLayout.NORTH);
		jframe.add(new JScrollPane(chatWindow),BorderLayout.CENTER);
		jframe.add(usersAndChatroomsJpanel,BorderLayout.EAST);
		jframe.add(inputAndSendJpanel,BorderLayout.SOUTH);

		jframe.setSize(800, 450);
		jframe.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		jframe.setLocationByPlatform(true);
		jframe.setVisible(true);
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

	//update chats window
	private void showChats(final String message){
		SwingUtilities.invokeLater(
				new Runnable(){
					public void run(){
						chatrooms.setText(message);
					}
				}
		);
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

	private void sendMessage(String message,PrintStream printStream){
		printStream.println(message);
	}

	public void connect(String host, int port)
	{

		/* Our socket end */
		Socket clientSocket;
		// For reading from socket */
		BufferedReader inFromServer;

		/* Hold user input */
		String  modifiedSentence;
		nameOfClient = JOptionPane.showInputDialog("Enter your name:");

		SwingUtilities.invokeLater( new Runnable() {
			public void run() {
				try {
					UIManager.setLookAndFeel(
							UIManager.getSystemLookAndFeelClassName());
				} catch (Exception useDefault) {}
				initGUI();
			}
		});

		System.out.println("-- Client connecting to host/port " + host + "/" + port + " --");

		/* Connect to the server at the specified host/port */
		try {
			clientSocket = new Socket( host, port );

			/* Create a reading buffer to the socket */
			inFromServer = new BufferedReader(new InputStreamReader( clientSocket.getInputStream()) );


			inFromUser = new BufferedReader( new InputStreamReader( System.in ) );
			/* Create a writing buffer to the socket */
			outToServer = new PrintStream( clientSocket.getOutputStream(), true);
			ableToType(true);




		} catch (UnknownHostException e) {
			System.out.println("Can not locate host/port " + host + "/" + port);
			return;
		} catch (IOException e) {
			System.out.println("Could not establish connection to: " + host + "/" + port);
			return;
		}

		System.out.println("<-- Connection established  -->");
		try
		{
			//Start thread for input(client to server)
			t = new Thread(this);
			t.start();

			while ( true ) {


				/* Read the server's response */
				modifiedSentence = inFromServer.readLine();
				//When typed exit get out of server
				try
				{
					modifiedSentence.equals(null);
				}
				catch(NullPointerException e)
				{
					break;
				}
				String[] arrOfStr = modifiedSentence.split("@@");
				if (arrOfStr.length >1) {
					String specialMsg = arrOfStr[1];
					if (specialMsg.split("connectTO").length > 1) {
						String[] stringContainsIPandPort = specialMsg.split("connectTO");
						String[] ipAndPort = stringContainsIPandPort[1].split("##");
						System.out.println("port: "+ipAndPort[0]+" ip: "+ipAndPort[1]+" chat name:"+ipAndPort[2]);
						new ClientUDPThread(Integer.parseInt(ipAndPort[0]),ipAndPort[1],nameOfClient,ipAndPort[2],Integer.parseInt(ipAndPort[3]));
						continue;
					}
					else if (specialMsg.split("connectPM").length > 1) {
						String[] stringContainsIPandPort = specialMsg.split("connectPM");
						String[] ipAndPort = stringContainsIPandPort[1].split("##");
						System.out.println("port: "+ipAndPort[0]+" ip: "+ipAndPort[1]+" chat name:"+ipAndPort[2]);
						new PrivateChatThread(Integer.parseInt(ipAndPort[0]),ipAndPort[1],nameOfClient,ipAndPort[2]);
						continue;
					}
					else if(specialMsg.split("00nameAssigned00").length > 1){
						String[] inputMsg = specialMsg.split("00nameAssigned00");
						String serverAssignedName = inputMsg[1];
						if (!serverAssignedName.equals(nameOfClient))
							System.out.println("Server assigned name: "+serverAssignedName);
							nameOfClient = serverAssignedName;
						continue;
					}
					else if(specialMsg.split("00users00").length > 1){
						String[] stringContainsUsers = specialMsg.split("00users00");
						String[] users = stringContainsUsers[1].split("##");
						String usersToShow = "USERS";
						for (int i=0;i<users.length;i++){
							if (users[i].equals(nameOfClient)){
								usersToShow+= "\n"+users[i]+"(ME)";
							}
							else{
								usersToShow+= "\n"+users[i];
							}

						}
						showUsers(usersToShow);
						continue;
					}
					else if (specialMsg.split("00chats00").length > 1){
						String[] stringContainsChatrooms = specialMsg.split("00chats00");
						String[] chatrooms = stringContainsChatrooms[1].split("##");
						String chatroomsToShow = "CHATROOMS";
						for (int i=0;i<chatrooms.length;i++){
							chatroomsToShow+= "\n"+chatrooms[i];
						}
						showChats(chatroomsToShow);
						continue;
					}
					else if (specialMsg.split("00invite00").length > 1){
						String[] inputMessage = specialMsg.split("00invite00");
						String[] usernameAndChatRoom = inputMessage[1].split("##");
						Object[] options1 = { "Accept",
								"Decline" };
						JPanel panel = new JPanel();
						panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
						panel.add(new JLabel(usernameAndChatRoom[0]+" wants to enter chatroom: "+usernameAndChatRoom[1]));

						int result = JOptionPane.showOptionDialog(null, panel, "Create a new chat room!!!",
								JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE,
								null, options1, null);
						if (result == JOptionPane.YES_OPTION) {
							sendMessage("@@00specialInvite00"+usernameAndChatRoom[0]+"##yes"+"##"+usernameAndChatRoom[1]+"00specialInvite00@@",outToServer);
						}
						else if (result == JOptionPane.NO_OPTION){
							sendMessage("@@00specialInvite00"+usernameAndChatRoom[0]+"##no"+"##"+usernameAndChatRoom[1]+"00specialInvite00@@",outToServer);
						}
						continue;
					}
				}
				//When typed exit get out of server
				try
				{
					modifiedSentence.equals(null);
				}
				catch(NullPointerException e)
				{
					break;
				}


				/* Display echoed message from server */
				System.out.println("--> "+modifiedSentence);
				showMessage(modifiedSentence);
			}

			// Close all of our connections.
		} catch (IOException e) {
			System.out.println(e);
			System.out.println("I/O to socket failed: " + host);
			showMessage("I/O to socket failed: " + host);
			showMessage("SERVER TERMINATED CONNECTION");
		}
	}  /* End Connect Method */

	public static void main( String[] argv )
	{
		/* Holds the server's name */
		String server;
		/* Holds the server's port number  */
		int port;

		/* The first argument is the server's name */
		server = JOptionPane.showInputDialog("Enter ip to connect to:");
		//server = "192.168.21.193";
		/* The second argument the port that the server accepts connections */
		port = Integer.parseInt("4567");

		/* Create a new instance of the client */
		ClientTCPThread myclient = new ClientTCPThread();

		/* Make a connection. It should not return until the client exits */
		myclient.connect(server, port);

		System.out.println("<-- Client has exited -->");
	} /* End main method */

	@Override
	public void run() {
		try {
			outToServer.println("@@00init00"+nameOfClient+"00init00@@");
			while(true){
				/* Send the message to server */
				sentence = inFromUser.readLine();
				if(sentence.equals("exit")) {
					outToServer.println(sentence);
					break;
				}
				else {
					outToServer.println(sentence);
					sendMessage(sentence,outToServer);
				}


			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}