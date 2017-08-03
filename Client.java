import java.io.*;
import java.net.*;
import java.util.*;
class Client{

	public static String clientID;
	public static BufferedReader br;//Read from Server.
	public static PrintWriter pw;//Write to Server.
	public static PrintStream ps;
	
	public static int processCount = 0;

	public static void main(String args[]){
		try{
			Socket socket = new Socket("home",4444);
			
			br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			pw = new PrintWriter(socket.getOutputStream(),true);
			ps = new PrintStream(socket.getOutputStream());
			clientID = br.readLine();
			System.out.println("ClientID received as "+clientID);
			startReadThread();
			startWriteThread();

		}catch(Exception e){
			System.out.println("Exception in listen socket "+e);
			System.exit(-1);
		}
	}

	public static void startReadThread(){
		new Thread(new Runnable(){
		
			public void run(){
				try{
					//BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
					
					while(true){
						String serverResponse = br.readLine();
						System.out.println("\nServer : \n"+serverResponse);
						
						System.out.print("Enter command or .q to exit: ");
					}
				}
				catch(Exception e){
					System.out.println("Exception in listen socket Read Thread "+e);
					System.exit(1);
				}
			}
		
		}).start();		
	}

	public static void startWriteThread(){
		
		new Thread(new Runnable(){
		
			public void run(){
				try{
					Scanner sc = new Scanner(System.in);
					while(true){
						System.out.print("Enter command or .q to exit: ");
						String message = sc.nextLine();
					
						if(message.equals(".q")){
							System.out.println("Disconnected from Server.!!");
							pw.println(message);
							System.exit(-1);
						}
						else if(message == null){
							continue;
						}
						else{
							pw.println(clientID+" "+(clientID+processCount)+" "+message);
							processCount++;						
						}					
					}
				}
				catch(Exception e){
					System.out.println("Exception in listen socket Write Thread "+e);
					System.exit(1);
				}
			}
		}).start();
	}


}
