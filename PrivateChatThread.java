import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class PrivateChatThread implements Runnable {


    private DatagramSocket datagramSocket;
    private InetAddress serverAddr;
    private int port;
    private String myName;
    private String senderName;
    Thread t;

    private JFrame jframe;


    private JPanel inputAndSendJpanel;
    private JTextField userText;
    private JTextArea chatWindow;
    private JButton sendButton;
    private JLabel typing;
    private JPanel usersAndChatroomsJpanel;
    private JTextArea users;




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

    //update typing
    private void showTyping(String msg){
        SwingUtilities.invokeLater(
                new Runnable(){
                    public void run(){
                        typing.setText(senderName+" PRESSED ("+msg+")");
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
        } catch (IOException e) {
            e.printStackTrace();
        }

    }



    public void initGUI(String chatName){
        jframe = new JFrame(chatName);

        //INPUT AND SEND BUTTON
        inputAndSendJpanel = new JPanel();
        inputAndSendJpanel.setLayout(new FlowLayout());


        userText = new JTextField("", 20);
        KeyListener keyListener = new KeyListener() {
            public void keyPressed(KeyEvent keyEvent) {
                sendMessage("@@00TYPING00"+myName+"##"+ whatPressed(keyEvent)+"00TYPING00@@");
            }

            public void keyReleased(KeyEvent keyEvent) {

            }

            public void keyTyped(KeyEvent keyEvent) {

            }

            private String whatPressed(KeyEvent keyEvent) {
                int keyCode = keyEvent.getKeyCode();
                String keyText = KeyEvent.getKeyText(keyCode);
                System.out.println(keyText);
                return keyText;
            }
        };
        userText.addKeyListener(keyListener);
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

        usersAndChatroomsJpanel = new JPanel();
        usersAndChatroomsJpanel.setLayout(new BoxLayout(usersAndChatroomsJpanel, BoxLayout.Y_AXIS));
        users = new JTextArea();
        users.setFont(users.getFont().deriveFont(12f));
        users.setEditable(false);
        users.setBorder(new EmptyBorder(20, 20, 20, 20));
        users.setBackground(Color.GRAY);
        usersAndChatroomsJpanel.add(users);

        inputAndSendJpanel.add(userText);
        inputAndSendJpanel.add(sendButton);
        chatWindow = new JTextArea();
        chatWindow.setFont(chatWindow.getFont().deriveFont(12f));
        chatWindow.setEditable(false);
        typing = new JLabel(" ");
        typing.setFont(typing.getFont().deriveFont(12f));
        jframe.add(typing,BorderLayout.NORTH);
        jframe.add(usersAndChatroomsJpanel,BorderLayout.EAST);
        jframe.add(new JScrollPane(chatWindow),BorderLayout.CENTER);
        jframe.add(inputAndSendJpanel,BorderLayout.SOUTH);
        jframe.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosed(java.awt.event.WindowEvent windowEvent) {
                sendMessage("@@exitFROMchat@@");
            }
        });
        jframe.setSize(400, 250);
        jframe.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        jframe.setLocationByPlatform(true);
        jframe.setVisible(true);
    }

    public PrivateChatThread(int port, String serverIPAddress, String name, String chatName) throws IOException {
        this.myName = name;
        String[] inputMsg = chatName.split("PRIVATE MESSAGE");
        if (myName.equals(inputMsg[0]))
            senderName = inputMsg[1];
        else
            senderName = inputMsg[0];

        SwingUtilities.invokeLater( new Runnable() {
            public void run() {
                try {
                    UIManager.setLookAndFeel(
                            UIManager.getSystemLookAndFeelClassName());
                } catch (Exception useDefault) {}
                initGUI("PRIVATE CHAT WITH  ("+senderName+")");
            }
        });


        this.port = port;
        String hostname;

        /* First argument is the running server's myName */
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
                    if (specialMsg.split("00kickedOUT00").length > 1){
                        jframe.dispose();
                    }
                    else if(specialMsg.split("00users00").length > 1){
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
                    else if (specialMsg.split("#0#").length > 1){
                        String[] typed = specialMsg.split("#0#");
                        showTyping(typed[1]);
                        continue;
                    }
                }
                showMessage(lineFromServer );
            }
        }});
        threadOutput.start();


    }



    @Override
    public void run() {
        String lineToServer = null;
        sendMessage("@@00init00"+ myName +"00init00@@");
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
