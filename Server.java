import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;
/**
 * 
 * @author Zhenkun Wu-201298999-x7zw
 *
 */
public class Server {
	private int portNumber = 2048;
	private String wlecome = "Please enter your username:";
	private String accepted = "Your username has been accepted.";
	private HashSet<String> client = new HashSet<String>();
	private HashSet<PrintWriter> clientWriters = new HashSet<PrintWriter>();
	private HashSet<String> commandList = new HashSet<String>(){{
		add("//Quit");
		add("//IPAddress");
		add("//NumberOfClient");
		add("//ServerRunningTime");
		add("//ChatTime");
		add("//Help");
	}};
	private double startTime;
	private ServerSocket socket;
	/**
	 * 
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		Server server = new Server();
		server.start();
	}
	/**
	 * It is the start of the program. It will establish the connection 
	 * and listen from the client.
	 * @throws IOException
	 */
	private void start() throws IOException{
		socket = new ServerSocket(portNumber);
		System.out.println("Echo server at "+
		InetAddress.getLocalHost()+" is waiting for connections.");
		Socket s;
		Thread thread;
		try{
			while(true){
				s = socket.accept();
				thread = new Thread(new HandleSession(s));
				thread.start();
			}
		}catch(Exception e){
			System.out.println(e.getMessage());
		}
		finally{
			shutDown();
		}
	}
	/**
	 * It will close the connection and quit the program.
	 */
	private void shutDown() {
		try{
			socket.close();
			System.out.println("The server is being shut down.");
		}catch(Exception e){
			System.err.println("Problem shutting down the server.");
			System.err.println(e.getMessage());
		}
	}
	/**
	 * Create a new thread for the server program and set 
	 * some methods for the thread.
	 */
	private class HandleSession implements Runnable{
		Socket s;
		String clientName;
		BufferedReader fromClient = null;
		PrintWriter toClient = null;
		HandleSession(Socket s1){
			this.s = s1;
		}
		/**
		 * The process of the thread. 
		 * Firstly ,create the input stream and output stream.
		 * Then, get the username from the client.
		 * Finally, begin listen for the client message.
		 */
		public void run(){
			try{
				createStreams();
				getClientUsername();
				listenForClientMessages();
			}catch(IOException e){
				System.out.println(e);
			}
			finally{
				closeConnection();
			}
		}
		/**
		 * Create the input stream and the output stream for the threa.
		 * Add the output stream to the hashmap.
		 */
		private void createStreams(){
			try{
				fromClient = new BufferedReader(new
						InputStreamReader(s.getInputStream()));
				toClient = new PrintWriter(new
						OutputStreamWriter(s.getOutputStream()));
				clientWriters.add(toClient);
				System.out.println("One connection has been established.");
			}catch(IOException e){
				System.err.println("Exception in creatStreams(): "+e);
			}
		}
		/**
		 * The start of the program. 
		 */
		private void getClientUsername(){
			toClient.println(wlecome);
			toClient.flush();
			try{
				clientName = fromClient.readLine();
			}catch(IOException e){
				System.err.println("Exceotion in getClientUsername(): "+e);
			}
			if(clientName == null){
				return;
			}
			if(!client.contains(clientName)){
				client.add(clientName);
				toClient.println(accepted);
				toClient.flush();
				broadcast(clientName+" has enter the chat room.");
			}else{
				toClient.println("Sorry, this username is unaviable.");
				toClient.flush();
				getClientUsername();
			}
		}
		/**
		 * This method is used to listen for the messgae from the client.
		 * @throws IOException
		 */
		private void listenForClientMessages() throws IOException{
			String line;
			while(true){
				line = fromClient.readLine();
				if(line == null){
					break;
				}else if(commandList.contains(line)){
					command(line);
				}else{
					broadcast(clientName+" has said "+line);
				}
			}
		}
		/**
		 * This method will process the command from the client.
		 * @param line is the string from the client.
		 * @throws IOException
		 */
		private void command(String line) throws IOException{
			Double time;
		    switch(line){
		        default:
		            break;
		        case "//IPAddress":
		            toClient.println("The IP adress for the server is "+InetAddress.getLocalHost()+" .");
					toClient.flush();
					break;
				case "//NumberOfClient":
				    toClient.println("The server has serve for "+client.size()+" clients.");
					toClient.flush();
					break;
				case "//ServerRunningTime":
				    time = System.currentTimeMillis() - startTime;
					toClient.println("The server has been running for "+time+" ms.");
					toClient.flush();
					break;
				case "//ChatTime":
				    time = System.currentTimeMillis() - Double.parseDouble(fromClient.readLine());
					toClient.println(clientName+" has enter the chat room for "+time+" ms.");
					toClient.flush();
					break;
				case "//Help":
				    toClient.println("The command list is:");
					for(String element: commandList){
					    toClient.println(element);
					}
					toClient.flush();
					break;
				case "//Quit":
				    closeConnection();
				    break;
		    }
		}
		/**
		 * This method will broadcast the message to all the clients stored in the HashMap.
		 * @param message is the string from the client.
		 */
		private void broadcast(String message){
			for(PrintWriter writer: clientWriters){
				writer.println(message);
				writer.flush();
			}
			System.out.println(message);
		}
		/**
		 * This method is used to close the connection.
		 */
		private void closeConnection(){
			if(clientName!=null){
				client.remove(clientName);
				broadcast(clientName+" has left the chat room.");
			}
			if(toClient!=null){
				clientWriters.remove(toClient);
			}
			try{
				s.close();
				System.out.println("One connection has been closed.");
			}catch(IOException e){
				System.err.println("Exception when closing the connection.");
				System.err.println(e.getMessage());
			}
		}
	}
}