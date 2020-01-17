import java.io.PrintStream;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;


public class ChatRoomHandler {
    private List<PrintStream> streamFromServerToClient =  new ArrayList<PrintStream>();
    private List<String> ipAddressOfClient =  new ArrayList<String>();
    private List<String> nameOfClient =  new ArrayList<String>();
    private List<ChatRoomThread> chatRooms = new ArrayList<ChatRoomThread>();
    private List<Thread> threadsTimers = new ArrayList<Thread>();
    //20000 is 20 seconds
    private long timeToDeleteChatRoom = 20000;
    private Thread threadTimer;


    public ChatRoomHandler(){
        //This thread checks if a room is inactive form more than the timeToDeleteChatRoom and it closes it

        threadTimer = new Thread(new Runnable() { public void run() {

            while(true) {
                for (int i=0;i<get();i++){
                    if ((System.currentTimeMillis()-chatRooms.get(i).getStartTime()) > timeToDeleteChatRoom){
                        System.out.println("TIME");
                        chatRooms.get(i).sendToAll("The room is DELETED!There was no action from more than "+(timeToDeleteChatRoom/1000)+" seconds");
                        try {
                            Thread.sleep(3000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        chatRooms.get(i).sendToAll("@@00kickedOUT00CLOSEWINDOW00kickedOUT00@@");
                        if (!chatRooms.get(i).isPrivate())
                            sendToAllWithoutSenderName("Chat room ("+chatRooms.get(i).getChatRoomName()+") is deleted because there was no action for more than "+(timeToDeleteChatRoom/1000)+" seconds");
                        chatRooms.remove(i);

                        sendChatRoomNames();
                    }
                }
            }
        }});
        threadTimer.start();

    }

    public synchronized int get()
    {
        return chatRooms.size();
    }



    public void addClientOutPutStream(PrintStream printStream){
        streamFromServerToClient.add(printStream);
    }

    public void addClientIpAddress(String ip){
        ipAddressOfClient.add(ip);
    }

    public void addClientName(String name){
        nameOfClient.add(name);
    }

    public String findMessageSender(PrintStream printStream){
        for (int i =0;i<streamFromServerToClient.size();i++){
            if (streamFromServerToClient.get(i).equals(printStream)){
                return nameOfClient.get(i);
            }
        }

        return "Undefined";
    }

    //Sends message to all users
    public void sendToAll(String message,PrintStream printStream){
        String senderName = findMessageSender(printStream);
        for(int i =0;i<streamFromServerToClient.size();i++){
            streamFromServerToClient.get(i).println( senderName +": "+ message );
        }
    }

    public void sendToAllWithoutSenderName(String message) {
        for(int i =0;i<streamFromServerToClient.size();i++){
            streamFromServerToClient.get(i).println(  message );
        }
    }

    public void manipulateMessage(String message, PrintStream printStream) throws InterruptedException {

        String[] arrOfStr = message.split("@@");
        if (arrOfStr.length >1) {
            String specialMsg = arrOfStr[1];
            if(specialMsg.split("00init00").length > 1){
                initUser(specialMsg,printStream);
            }
            else if (specialMsg.split("00create00").length > 1){
                createChatRoom(specialMsg,printStream);
            }
            else if (specialMsg.split("00connect00").length > 1){
                connectChatRoom(specialMsg,printStream);
            }
            else if(specialMsg.split("00delete00").length > 1){
                deleteChatRoom(specialMsg,printStream);
            }
            else if(specialMsg.split("00changeowner00").length > 1){
                changeChatRoomOwner(specialMsg,printStream);
            }
            else  if(specialMsg.split("00specialInvite00").length > 1){
                connectWithInvitation(specialMsg,printStream);
            }
            else if(specialMsg.split("00privateChat00").length > 1){
                privateChat(specialMsg,printStream);
            }
        }
        else {
            sendToAll(message,printStream);
        }
    }

    public void initUser(String specialMsg,PrintStream printStream){
        String[] client = specialMsg.split("00init00");
        String name = client[1];
        boolean userExists = true;
        while(userExists && nameOfClient.size()>0){
            for (int i =0;i< nameOfClient.size();i++){
                if (nameOfClient.get(i).equals(name)){
                    name += "!";
                    break;
                }
                if (i==nameOfClient.size()-1)
                    userExists = false;
            }
        }
        printStream.println("@@00nameAssigned00"+name+"00nameAssigned00@@");
        addClientName(name);
        sendUsersNames();
        sendChatRoomNames();
        sendToAllWithoutSenderName(name+" logged in the SERVER");

    }

    public void sendUsersNames(){
        String usersInSystem = "@@00users00";
        if (nameOfClient.size()==1){
            usersInSystem += nameOfClient.get(0)+"00users00@@";
            sendToAllWithoutSenderName(usersInSystem);
        }
        else {
            for (int i = 0; i < nameOfClient.size(); i++) {
                if (i==0){
                    usersInSystem += nameOfClient.get(0);
                }
                else{
                    usersInSystem += "##"+nameOfClient.get(i);
                }
            }
            usersInSystem += "00users00@@";
            sendToAllWithoutSenderName(usersInSystem);
        }
    }

    public void sendChatRoomNames(){
        String chatsInSystem = "@@00chats00";
        if (chatRooms.size()==0){
            chatsInSystem += "No chat rooms available"+"00chats00@@";
            sendToAllWithoutSenderName(chatsInSystem);
        }
        else if (chatRooms.size()==1){
            if (!chatRooms.get(0).isPrivate()) {
                chatsInSystem += chatRooms.get(0).getChatRoomName() + "00chats00@@";
                sendToAllWithoutSenderName(chatsInSystem);
            }
            else{
                chatsInSystem += "No chat rooms available"+"00chats00@@";
                sendToAllWithoutSenderName(chatsInSystem);
            }
        }
        else {
            for (int i = 0; i < chatRooms.size(); i++) {
                if (!chatRooms.get(i).isPrivate()) {
                    if (i == 0) {
                        chatsInSystem += chatRooms.get(0).getChatRoomName();
                    } else {
                        chatsInSystem += "##" + chatRooms.get(i).getChatRoomName();
                    }
                }
            }
            if (chatsInSystem.equals("@@00chats00")){
                chatsInSystem += "No chat rooms available"+"00chats00@@";
                sendToAllWithoutSenderName(chatsInSystem);
            }
            else{
                chatsInSystem += "00chats00@@";
                sendToAllWithoutSenderName(chatsInSystem);
            }

        }
    }

    public void createChatRoom(String specialMsg,PrintStream printStream){
        String[] inputMsg = specialMsg.split("00create00");
        String[] nameAndpassAndTimetoKickedAndInvitation = inputMsg[1].split("##");
        boolean chrmExists = false;
        for (int i=0;i<chatRooms.size();i++){
            if (nameAndpassAndTimetoKickedAndInvitation[0].equals(chatRooms.get(i).getChatRoomName())){
                chrmExists = true;
            }
        }
        if (!chrmExists) {
            chatRooms.add(new ChatRoomThread(nameAndpassAndTimetoKickedAndInvitation[0], findMessageSender(printStream),nameAndpassAndTimetoKickedAndInvitation[1],printStream,Integer.parseInt(nameAndpassAndTimetoKickedAndInvitation[2])*1000,Boolean.parseBoolean(nameAndpassAndTimetoKickedAndInvitation[3]),false));
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            int port = chatRooms.get(chatRooms.size() - 1).getPortServer();
            String ipCorrect = getCorrectIp();
            System.out.println("@@connectTO" + port + "##" + ipCorrect + "##" + nameAndpassAndTimetoKickedAndInvitation[0] +"##"+ nameAndpassAndTimetoKickedAndInvitation[2]+"000"+ "connectTO@@");
            printStream.println("@@connectTO" + port + "##" + ipCorrect + "##" + nameAndpassAndTimetoKickedAndInvitation[0]+"##"+ nameAndpassAndTimetoKickedAndInvitation[2]+"000" + "connectTO@@");
            sendToAllWithoutSenderName("Chat room with name --("+chatRooms.get(chatRooms.size() - 1).getChatRoomName()+")-- is created!!!");
            chatRooms.get(chatRooms.size() - 1).addUniqueClientIdentifier(printStream);
            sendChatRoomNames();
        }
        else{
            printStream.println("Chat room with name --("+nameAndpassAndTimetoKickedAndInvitation[0]+")-- ALREADY EXISTS!!!");
        }
    }

    public void connectChatRoom(String specialMsg,PrintStream printStream){
        String[] inputMessage = specialMsg.split("00connect00");
        String[] nameAndpass = inputMessage[1].split("##");
        int chatRoomID = -1;
        for (int i = 0;i<chatRooms.size();i++){
            if (chatRooms.get(i).getChatRoomName().equals(nameAndpass[0])){
                chatRoomID = i;
            }
        }
        if (chatRoomID >= 0){
            int port = chatRooms.get(chatRoomID).getPortServer();
            String ipCorrect = getCorrectIp();
            if (!chatRooms.get(chatRoomID).isBanned(printStream)) {
                if (!chatRooms.get(chatRoomID).isLogedIn(printStream)) {
                    if(chatRooms.get(chatRoomID).isInvitation()){
                        if (chatRooms.get(chatRoomID).getCreatorUniqueIdentifier().equals(printStream)){
                            System.out.println("@@connectTO" + port + "##" + ipCorrect + "##" + nameAndpass[0] + "##" + chatRooms.get(chatRoomID).getTimeForUserTiKicked() + "connectTO@@");
                            printStream.println("@@connectTO" + port + "##" + ipCorrect + "##" + nameAndpass[0] + "##" + chatRooms.get(chatRoomID).getTimeForUserTiKicked() + "connectTO@@");
                            chatRooms.get(chatRoomID).addUniqueClientIdentifier(printStream);
                        }
                        else{
                            System.out.println("@@00invite00"+findMessageSender(printStream)+"##"+nameAndpass[0]+"00invite00@@");
                            printStream.println("This room accepts users with invitation!Message has been sent to creator,wait for acceptance!");
                            chatRooms.get(chatRoomID).getCreatorUniqueIdentifier().println("@@00invite00"+findMessageSender(printStream)+"##"+nameAndpass[0]+"00invite00@@");
                        }
                    }
                    else {
                        if (chatRooms.get(chatRoomID).getPassword().equals(nameAndpass[1])) {
                            System.out.println("@@connectTO" + port + "##" + ipCorrect + "##" + nameAndpass[0] + "##" + chatRooms.get(chatRoomID).getTimeForUserTiKicked() + "connectTO@@");
                            printStream.println("@@connectTO" + port + "##" + ipCorrect + "##" + nameAndpass[0] + "##" + chatRooms.get(chatRoomID).getTimeForUserTiKicked() + "connectTO@@");
                            chatRooms.get(chatRoomID).addUniqueClientIdentifier(printStream);
                        } else {
                            printStream.println("Wrong password");
                        }
                    }
                }
                else{
                    printStream.println("You are already LOGGED IN!!");
                }
            }
            else
                printStream.println("You are banned from this chat room you cannot connect!!!");
        }
        else{
            printStream.println("Chat room not found");
        }
    }

    public void connectWithInvitation(String specialMsg,PrintStream printStream){
        String[] inputMessage = specialMsg.split("00specialInvite00");
        String[] nameAndAcceptanceAndChatName = inputMessage[1].split("##");
        PrintStream userPrintstream = null;
        for (int i = 0;i<nameOfClient.size();i++){
            if (nameOfClient.get(i).equals(nameAndAcceptanceAndChatName[0])){
                userPrintstream = streamFromServerToClient.get(i);
            }
        }
        if (nameAndAcceptanceAndChatName[1].equals("yes")){
            int chatRoomID = -1;
            for (int i = 0;i<chatRooms.size();i++){
                if (chatRooms.get(i).getChatRoomName().equals(nameAndAcceptanceAndChatName[2])){
                    chatRoomID = i;
                }
            }

            if (chatRoomID >= 0) {
                int port = chatRooms.get(chatRoomID).getPortServer();
                String ipCorrect = getCorrectIp();

                System.out.println("@@connectTO" + port + "##" + ipCorrect + "##" + nameAndAcceptanceAndChatName[2] + "##" + chatRooms.get(chatRoomID).getTimeForUserTiKicked() + "connectTO@@");
                userPrintstream.println("@@connectTO" + port + "##" + ipCorrect + "##" + nameAndAcceptanceAndChatName[2] + "##" + chatRooms.get(chatRoomID).getTimeForUserTiKicked() + "connectTO@@");
                chatRooms.get(chatRoomID).addUniqueClientIdentifier(userPrintstream);
            }
        }
        else{
            System.out.println("Creator did not accept you in the chatroom!!");
            userPrintstream.println("Creator did not accept you in the chatroom!!");
        }
    }

    public void deleteChatRoom(String specialMsg,PrintStream printStream){
        String[] chatRoomName = specialMsg.split("00delete00");
        boolean chrmFound = false;
        int chatRoomID = -1;
        for (int i=0;i<chatRooms.size();i++){
            if (chatRoomName[1].equals(chatRooms.get(i).getChatRoomName())){
                chrmFound = true;
                chatRoomID = i;
            }
        }
        if (chrmFound) {
            if (chatRooms.get(chatRoomID).getCreator().equals(findMessageSender(printStream)) || chatRooms.get(chatRoomID).isPrivate()) {
                if (! chatRooms.get(chatRoomID).isPrivate()) {
                    chatRooms.get(chatRoomID).sendToAll("The room is DELETED!Window will close in 3 seconds!");
                    try {
                        Thread.sleep(3000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    chatRooms.get(chatRoomID).sendToAll("@@00kickedOUT00CLOSEWINDOW00kickedOUT00@@");
                    chatRooms.remove(chatRoomID);
                    System.out.println("Chat room with name --(" + chatRoomName[1] + ")-- is DELETED!!!");
                    sendToAllWithoutSenderName("Chat room with name --(" + chatRoomName[1] + ")-- is DELETED!!!");
                    sendChatRoomNames();
                }
                else{
                    chatRooms.get(chatRoomID).sendToAll("The private chat is DELETED!Window will close in 3 seconds!");
                    try {
                        Thread.sleep(3000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    chatRooms.get(chatRoomID).sendToAll("@@00kickedOUT00CLOSEWINDOW00kickedOUT00@@");
                    chatRooms.remove(chatRoomID);
                    System.out.println("Chat room with name --(" + chatRoomName[1] + ")-- is DELETED!!!");
                }
            }
            else{
                System.out.println("YOU ARE NOT THR CREATOR, YOU CANNOT DELETE THE CHAT ROOM!!!");
                printStream.println("YOU ARE NOT THR CREATOR, YOU CANNOT DELETE THE CHAT ROOM!!!");
            }
        }
        else{
            printStream.println("Chat room with name: "+chatRoomName[1]+" NOT FOUND!!!");
        }
    }

    public void changeChatRoomOwner(String specialMsg,PrintStream printStream){
        String[] chatRoomonwer = specialMsg.split("00changeowner00");
        String[] ownerAndChatRoom = chatRoomonwer[1].split("##");
        boolean chrmFound = false;
        int chatRoomID = 0;
        for (int i=0;i<chatRooms.size();i++){
            if (ownerAndChatRoom[1].equals(chatRooms.get(i).getChatRoomName())){
                chrmFound = true;
                chatRoomID = i;
            }
        }
        if (chrmFound) {
            if (chatRooms.get(chatRoomID).getCreatorUniqueIdentifier().equals(printStream)) {
                boolean foundUser = false;
                int foundUserID =0;

                for (int i=0;i<nameOfClient.size();i++){
                    if (nameOfClient.get(i).equals(ownerAndChatRoom[0])){
                        foundUser = true;
                        foundUserID =i;
                    }
                }
                if (foundUser) {
                    chatRooms.get(chatRoomID).setCreator(ownerAndChatRoom[0]);
                    chatRooms.get(chatRoomID).setCreatorUniqueIdentifier(streamFromServerToClient.get(foundUserID));
                    System.out.println("The new owner of " + ownerAndChatRoom[1] + " is " + ownerAndChatRoom[0]);
                    chatRooms.get(chatRoomID).sendUsersNames();
                    printStream.println("The new owner of " + ownerAndChatRoom[1] + " is " + ownerAndChatRoom[0]);
                }
                else{
                    System.out.println("User " +  ownerAndChatRoom[0]+" NOT FOUND");
                    printStream.println("User " +  ownerAndChatRoom[0]+" NOT FOUND");
                }
            }
            else{
                System.out.println("YOU ARE NOT THR CREATOR YOU CANNOT CHANGE OWNERSHIP!!!");
                printStream.println("YOU ARE NOT THR CREATOR YOU CANNOT CHANGE OWNERSHIP!!!");
            }
        }
        else{
            printStream.println("Chat room with name: "+ownerAndChatRoom[1]+" NOT FOUND!!!");
        }
    }

    public void privateChat(String specialMsg,PrintStream printStream){
        String[] inputMsg = specialMsg.split("00privateChat00");
        String[]  fromPmTo = inputMsg[1].split("##");
        String from = fromPmTo[0];
        String to = fromPmTo[1];
        String chatName = from+"PRIVATE MESSAGE"+to;
        String chatReverse = to+"PRIVATE MESSAGE"+from;
        boolean chrmExists = false;
        int chatID = 0;
        for (int i=0;i<chatRooms.size();i++){
            if (chatName.equals(chatRooms.get(i).getChatRoomName()) || chatReverse.equals(chatRooms.get(i).getChatRoomName())){
                chrmExists = true;
                chatID = i;
            }
        }
        if (to.equals(findMessageSender(printStream))){
            System.out.println("You cannot have private chat with yourself,this is not personal psychotherapy");
            printStream.println("You cannot have private chat with yourself,this is not personal psychotherapy");
            return;
        }
        if (!chrmExists) {
            PrintStream userPrintstream = null;
            for (int i = 0; i < nameOfClient.size(); i++) {
                if (nameOfClient.get(i).equals(to)) {
                    userPrintstream = streamFromServerToClient.get(i);
                }
            }
            if (userPrintstream == null) {
                System.out.println("User not found!!");
                printStream.println("User not found!!");
                return;
            }

            chatRooms.add(new ChatRoomThread(chatName, findMessageSender(printStream), "empty", printStream, 0, false, true));
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            int port = chatRooms.get(chatRooms.size() - 1).getPortServer();
            String ipCorrect = getCorrectIp();
            System.out.println("@@connectPM" + port + "##" + ipCorrect + "##" + chatName + "##" + 0 + "000" + "connectPM@@");
            printStream.println("@@connectPM" + port + "##" + ipCorrect + "##" + chatName + "##" + 0 + "000" + "connectPM@@");
            chatRooms.get(chatRooms.size() - 1).addUniqueClientIdentifier(printStream);

            System.out.println("@@connectPM" + port + "##" + ipCorrect + "##" + chatName + "##" + 0 + "000" + "connectPM@@");
            userPrintstream.println("@@connectPM" + port + "##" + ipCorrect + "##" + chatName + "##" + 0 + "000" + "connectPM@@");
            chatRooms.get(chatRooms.size() - 1).addUniqueClientIdentifier(userPrintstream);
        }
        else{
            if(!chatRooms.get(chatID).isLogedIn(printStream)){
                int port = chatRooms.get(chatID).getPortServer();
                String ipCorrect = getCorrectIp();
                System.out.println("@@connectPM" + port + "##" + ipCorrect + "##" + chatName + "##" + 0 + "000" + "connectPM@@");
                printStream.println("@@connectPM" + port + "##" + ipCorrect + "##" + chatName + "##" + 0 + "000" + "connectPM@@");
                chatRooms.get(chatID).addUniqueClientIdentifier(printStream);
                 String theOtherUser;
                if (from.equals(findMessageSender(printStream))){
                    theOtherUser = to;
                }
                else{
                    theOtherUser = from;
                }
                PrintStream userPrintstream = null;
                for (int i = 0; i < nameOfClient.size(); i++) {
                    if (nameOfClient.get(i).equals(theOtherUser)) {
                        userPrintstream = streamFromServerToClient.get(i);
                    }
                }
                if(!chatRooms.get(chatID).isLogedIn(userPrintstream)){
                    System.out.println("@@connectPM" + port + "##" + ipCorrect + "##" + chatName + "##" + 0 + "000" + "connectPM@@");
                    userPrintstream.println("@@connectPM" + port + "##" + ipCorrect + "##" + chatName + "##" + 0 + "000" + "connectPM@@");
                    chatRooms.get(chatID).addUniqueClientIdentifier(userPrintstream);
                }

            }
            else{
                System.out.println("Already chatting privately with "+to);
                printStream.println("Already chatting privately with "+to);
            }

        }
    }

    public String getCorrectIp(){
        String ipCorrect = null;
        try(final DatagramSocket socket = new DatagramSocket()){
            socket.connect(InetAddress.getByName("8.8.8.8"), 10002);
            ipCorrect = socket.getLocalAddress().getHostAddress();
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

        return  ipCorrect;
    }

    //This is for when users log out of room
    //It deletes every room he has created and also log him out from where he is loged in
    public void deleteUser(PrintStream printStream){
        for (int i =0;i<streamFromServerToClient.size();i++){
            if (streamFromServerToClient.get(i).equals(printStream)){
                for (int chatID=0;chatID<chatRooms.size();chatID++){
                    String[] fromprivateto = chatRooms.get(chatID).getChatRoomName().split("PRIVATE MESSAGE");
                    if (chatRooms.get(chatID).isLogedIn(printStream)){
                        int port = chatRooms.get(chatID).getPortOfUniqueIdentifier(printStream);
                        if (port >= 0 ) {
                            chatRooms.get(chatID).deleteUser(port);
                            chatRooms.get(chatID).sendUsersNames();
                        }
                    }
                    if (chatRooms.get(chatID).getCreatorUniqueIdentifier().equals(printStream)) {
                        sendToAllWithoutSenderName("The rooms that " + nameOfClient.get(i) + " has created are DELETED!");
                        deleteChatRoom("00delete00" + chatRooms.get(chatID).getChatRoomName() + "00delete00", printStream);
                    }
                    else if(fromprivateto.length > 1){
                        if(findMessageSender(printStream).equals(fromprivateto[1])){
                            deleteChatRoom("00delete00" + chatRooms.get(chatID).getChatRoomName() + "00delete00", printStream);
                        }
                    }
                }
                streamFromServerToClient.remove(i);
                ipAddressOfClient.remove(i);
                sendToAllWithoutSenderName( nameOfClient.get(i)+" logged out of the SERVER!!");
                nameOfClient.remove(i);
                sendUsersNames();
            }
        }
    }
}
