import java.io.*; 
import java.net.*;

public class ConnectionHandler implements Runnable
{
	// Socket for our endpoint
	protected Socket echoSocket;

	public static ChatRoomHandler chatRoomHandler = new ChatRoomHandler();
	
	public ConnectionHandler(Socket aSocketToHandle)
	{
		echoSocket = aSocketToHandle;
	}

	/**  * New thread for handling client interaction will start here. */
    public void run(  )
        {
			// Holds messages we get from client
			String clientSentence = "";
			// Input object
			BufferedReader inFromClient; 
		    // Output object
			PrintStream outToClient;
			// Client's name
			String peerName;            

            // Attach a println/readLine interface to the socket so we can read and write strings to the socket.
            try {
                /* Get the IP address from the client */
                peerName = echoSocket.getInetAddress().getHostAddress();
				/* Create a writing stream to the socket */
				outToClient = new PrintStream( echoSocket.getOutputStream(), true );				
				/* Create a reading stream to the socket */
				inFromClient = new BufferedReader( new InputStreamReader( echoSocket.getInputStream() ) );

				chatRoomHandler.addClientOutPutStream(outToClient);
				chatRoomHandler.addClientIpAddress(peerName);
            }
            catch (IOException e) {
                System.out.println("Error creating buffered handles.");
                return;
            }
	  
           System.out.println("Handling connection to client at " + peerName + " --");

            outToClient.println("---- Welcome to IRC!!! ----");

            while ( true )
            {
				try {
					/* Read client's message through the socket's input buffer */
					
						clientSentence = inFromClient.readLine();
					
				}
				catch (IOException e) {
					System.out.println( echoSocket.getInetAddress() + "-" + peerName + " broke the connection." );
					break;
				}

				/* Output to screen the message received by the client */
				System.out.println( "Message Received: " + clientSentence );


				/* If message is exit then terminate specific connection - exit the loop */
				if ( clientSentence.equals( "exit" ) ) {
					System.out.println( "Closing connection with " + echoSocket.getInetAddress() + "." );
					break;
				}
				

				/* Send it back through socket's output buffer */
				try {
					chatRoomHandler.manipulateMessage(clientSentence,outToClient);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}

            System.out.println("Closing " + peerName + " connection");
			chatRoomHandler.deleteUser(outToClient);
            // Close all the handles
            try {
				/* Close input stream */
				inFromClient.close();
				/* Close output stream */
				outToClient.close();
				/* Close TCP connection with client on specific port */
				echoSocket.close();
            }
            catch (IOException e) {
            }
	}  /* End run method */

} // end class

