import java.io.IOException;
import java.io.PrintStream;
import java.net.*;
import java.util.ArrayList;
import java.util.List;

public class ChatRoomThread implements Runnable{
    DatagramSocket socket = null;
    private String ChatRoomName;
    private String creator;
    private PrintStream creatorUniqueIdentifier;
    private String password;
    private String ipServer;
    private boolean invitation;
    private int portServer;
    private List<String> users = new ArrayList<String>();
    private List<Integer> ports = new ArrayList<Integer>();
    private List<InetAddress> ips = new ArrayList<InetAddress>();
    private List<PrintStream> uniqueClientIdentifier =  new ArrayList<PrintStream>();
    private List<PrintStream> bannedClients = new ArrayList<PrintStream>();
    private int timeForUserTiKicked;
    private long startTime;
    private boolean isPrivate;
    Thread t;



    public ChatRoomThread(String chatRoomName, String creator, String password, PrintStream creatorUniqueIdentifier, int timeForUserTiKicked,boolean invitation,boolean isPrivate) {
        ChatRoomName = chatRoomName;
        this.creator = creator;
        this.creatorUniqueIdentifier = creatorUniqueIdentifier;
        this.password = password;
        this.invitation = invitation;
        t = new Thread(this);
        t.start();
        startTime = System.currentTimeMillis();
        this.timeForUserTiKicked = timeForUserTiKicked;
        this.isPrivate = isPrivate;
    }

    public boolean isInvitation() {
        return invitation;
    }

    public void setInvitation(boolean invitation) {
        this.invitation = invitation;
    }

    public boolean isPrivate() {
        return isPrivate;
    }

    public void setPrivate(boolean aPrivate) {
        isPrivate = aPrivate;
    }

    public int getTimeForUserTiKicked() {
        return timeForUserTiKicked;
    }

    public void setTimeForUserTiKicked(int timeForUserTiKicked) {
        this.timeForUserTiKicked = timeForUserTiKicked;
    }

    public void setCreatorUniqueIdentifier(PrintStream creatorUniqueIdentifier) {
        this.creatorUniqueIdentifier = creatorUniqueIdentifier;
    }

    public PrintStream getCreatorUniqueIdentifier() {
        return creatorUniqueIdentifier;
    }

    public List<PrintStream> getUniqueClientIdentifier() {
        return uniqueClientIdentifier;
    }

    public int getPortOfUniqueIdentifier(PrintStream printStream){
        for (int i =0;i<uniqueClientIdentifier.size();i++){
            if (printStream.equals(uniqueClientIdentifier.get(i)))
                return ports.get(i);
        }
        return -1;

    }

    public void addPort(int port){
        ports.add(port);
    }

    public void addIP(InetAddress ip){
        ips.add(ip);
    }

    public void addUniqueClientIdentifier(PrintStream printStream){
        for (int i =0;i<uniqueClientIdentifier.size();i++){
            if (uniqueClientIdentifier.get(i).equals(printStream))
                return;
        }
        uniqueClientIdentifier.add(printStream);
    }

    public boolean isLogedIn(PrintStream printStream){
        for (int i =0;i<uniqueClientIdentifier.size();i++){
            if (printStream.equals(uniqueClientIdentifier.get(i)))
                return true;
        }
        return false;
    }

    public void addBannedClient(PrintStream printStream){
        bannedClients.add(printStream);
    }

    public boolean isBanned(PrintStream printStream){
        for (int i=0;i<bannedClients.size();i++){
            if (bannedClients.get(i).equals(printStream))
                return true;
        }
        return false;
    }

    public boolean existsPort(int port){
        for (Integer portsToCompare : ports ) {
            if (portsToCompare.equals(port))
                return true;
        }
        return false;
    }


    public void sendToAll(String message){
        /* Create a new datagram data buffer (byte array) for echoing capitalized message */
        byte[] msgToClient = message.getBytes();
        for (int i =0;i<ports.size();i++){
            /* Create an outgoing datagram by extracting the client's address and portServer from incoming datagram */
            DatagramPacket outdatagram = new DatagramPacket(msgToClient, msgToClient.length,
                    ips.get(i), ports.get(i));

            try {
                socket.send(outdatagram);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void sendMessageToSpecificPort(String message,int port){
        byte[] msgToClient = message.getBytes();
        int userID=0;
        for (int i =0;i<ports.size();i++){
            if (port == ports.get(i))
                userID = i;
        }
        /* Create an outgoing datagram by extracting the client's address and portServer from incoming datagram */
        DatagramPacket outdatagram = new DatagramPacket(msgToClient, msgToClient.length,
                ips.get(userID), ports.get(userID));
        try {
            socket.send(outdatagram);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void deleteUser(int portNum){
        int userID = -1;
        for (int i=0;i<ports.size();i++){
            if (ports.get(i) == portNum){
                userID = i;
            }
        }
        sendToAll(users.get(userID)+" left the chat room!!!");
        users.remove(userID);
        ports.remove(userID);
        ips.remove(userID);
        uniqueClientIdentifier.remove(userID);
    }

    public void kickUser(String clientName,int port){
        int creatorID = -1;
        for (int i=0;i<uniqueClientIdentifier.size();i++){
            if (uniqueClientIdentifier.get(i).equals(creatorUniqueIdentifier))
                creatorID = i;
        }

        //Check if the creator wants to kick
        if (ports.get(creatorID)== port){
            for (int j=0;j<users.size();j++){
                if (clientName.equals(users.get(j))){
                    if (ports.get(j)==ports.get(creatorID)){
                        sendMessageToSpecificPort("Creator cannot be kicked",ports.get(j));
                        return;
                    }
                    sendMessageToSpecificPort("You are kicked out of the room,message will not be send anymore!!",ports.get(j));
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    //This message triggers on the client side to close the chat window
                    sendMessageToSpecificPort("@@00kickedOUT00CLOSEWINDOW00kickedOUT00@@",ports.get(j));

                    users.remove(j);
                    ports.remove(j);
                    ips.remove(j);
                    bannedClients.add(uniqueClientIdentifier.get(j));
                    uniqueClientIdentifier.remove(j);
                    sendToAll(clientName+" was kicked out!!!!");
                    return;
                }
            }
            sendMessageToSpecificPort("User ("+clientName+") not found!!!",port);
            return;
        }
        else
            sendMessageToSpecificPort("You are not the creator,you cannot kick!!!",port);


    }

    public long getStartTime() {
        return startTime;
    }

    public int getPortServer() {
        return portServer;
    }

    public void setPortServer(int portServer) {
        this.portServer = portServer;
    }

    public String getChatRoomName() {
        return ChatRoomName;
    }


    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }


    public void setIpServer(String ipServer) {
        this.ipServer = ipServer;
    }

    public List<String> getUsers() {
        return users;
    }

    public void addUser(String user) {
        users.add(user);
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password,String user) {
        if (users.get(0).equals(user))
            this.password = password;

    }

    public void sendUsersNames(){

        String usersInSystem = "@@00users00";
        if (users.size() == 1) {
            if (users.get(0).equals(creator) && !isPrivate)
                usersInSystem += users.get(0) + "(CREATOR)";
            else
                usersInSystem += users.get(0);
            usersInSystem += "00users00@@";
            sendToAll(usersInSystem);
        } else {
            for (int i = 0; i < users.size(); i++) {
                if (i == 0) {
                    if (users.get(i).equals(creator) && !isPrivate)
                        usersInSystem += users.get(0) + "(CREATOR)";
                    else
                        usersInSystem += users.get(0);
                } else {
                    if (users.get(i).equals(creator) && !isPrivate)
                        usersInSystem += "##" + users.get(i) + "(CREATOR)";
                    else
                        usersInSystem += "##" + users.get(i);
                }
            }
            usersInSystem += "00users00@@";
            sendToAll(usersInSystem);
        }

    }

    public void sendMessageExceptUser(String name,String msg){
        for (int i=0;i<users.size();i++){
            if (!users.get(i).equals(name))
                sendMessageToSpecificPort(msg,ports.get(i));
        }
    }



    @Override
    public void run() {
        String sentence = null;
        int maxLength = 255;

        /* Create a receiving datagram data buffer */
        byte[] buffer = new byte[255];
        InetAddress thisMachine = null;

        try {
            thisMachine = InetAddress.getLocalHost();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

        /* Create receiving datagram object of maximum size maxLength */
        DatagramPacket indatagram = new DatagramPacket(buffer, maxLength);

        /* Create a UDP socket on a specific portServer */

        try {
            socket = new DatagramSocket();
        } catch (SocketException e) {
            e.printStackTrace();
        }

        /* Display message that the UDP echo server is running */
        System.out.println("Starting a UDP Echo Server on portServer " + socket.getLocalPort() +" with ipServer: "+thisMachine.getHostAddress());

        setPortServer(socket.getLocalPort());
        setIpServer(thisMachine.getHostAddress());


        while (true) {
            /* Set the max length of datagram to 255 */
            indatagram.setLength(maxLength);

            /* Receive the datagram from the client  */
            try {
                socket.receive(indatagram);
                startTime = System.currentTimeMillis();
            } catch (IOException e) {
                e.printStackTrace();
            }

            //Add user in the chatroom(initialize)
            if (!existsPort(indatagram.getPort())) {

                addPort(indatagram.getPort());
                System.out.println("port added");

                addIP(indatagram.getAddress());
                System.out.println("address added");

                String msgFromClient = new String(indatagram.getData(), 0, indatagram.getLength());
                String[] arrOfStr = msgFromClient.split("@@");
                if (arrOfStr.length >1) {
                    String specialMsg = arrOfStr[1];
                    if (specialMsg.split("00init00").length > 1) {
                        String[] nameOfClient = specialMsg.split("00init00");
                        addUser(nameOfClient[1]);
                        sendToAll(nameOfClient[1] + " logged in!!");
                        sendUsersNames();
                    }
                }
                continue;
            }


            /* Convert the message from the byte array to a string array for displaying */
            String msgFromClient = new String(indatagram.getData(), 0, indatagram.getLength());
            String[] arrOfStr = msgFromClient.split("@@");
            //Special message to check if users exits or a user has to be kicked
            if (arrOfStr.length >1) {
                String specialMsg = arrOfStr[1];
                System.out.println(specialMsg);
                if(specialMsg.equals("exitFROMchat")){
                    deleteUser(indatagram.getPort());
                    sendUsersNames();
                    continue;
                }
                if (specialMsg.split("00kick00").length > 1){
                    String[] msg = specialMsg.split("00kick00");
                    String clientToKick = msg[1];
                    kickUser(clientToKick,indatagram.getPort());
                    sendUsersNames();
                    continue;
                }
                if(specialMsg.split("00TYPING00").length > 1){
                    String[] msg = specialMsg.split("00TYPING00");
                    String[] nameandKeyPressed = msg[1].split("##");
                    String userTyping = nameandKeyPressed[0];
                    String keyPressed = nameandKeyPressed[1];
                    sendMessageExceptUser(userTyping,"@@#0#"+keyPressed+"#0#@@");
                    continue;
                }
            }
            /* Display the message on the screen */
            System.out.println("\nMessage received from " + indatagram.getAddress() + " from portServer "
                    + indatagram.getPort() + ".\nContent: " + msgFromClient);

            //Message that will be sent to all group members
            sentence = msgFromClient;

            for (int i =0;i<ports.size();i++){
                if (indatagram.getPort() == ports.get(i)){
                    sendToAll("["+users.get(i)+"]"+sentence);
                    break;
                }
            }
        }
    }
}
