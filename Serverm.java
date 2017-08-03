import java.io.*;
import java.net.*;
import java.util.*;

class ConnectionHandler implements Runnable{
	
	private static final String QUIT_COMMAND = ".q";

	Socket clientSocket;
	String clientID;
	ConnectionHandler(Socket client,String clientID){
		clientSocket = client;
		this.clientID = clientID;
	}

	public void run(){
		try{
		BufferedReader br = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
		PrintWriter pw = new PrintWriter(clientSocket.getOutputStream(),true);
		pw.println(clientID);

		while(true){
			String message = br.readLine();
			if(!(message.equals(QUIT_COMMAND))){
				System.out.println("Client : "+message);
				
				Server.executeProcess(message);
			}
			else{
				System.out.println("Shutting Down a Connection..!!");
				break;
			}
			//pw.println("Server Echoing Back Message : "+message);
			//pw.flush();
		}
		}catch(Exception e){
			System.out.println("ConnectionHandler -- Exception TRACE -");
			e.printStackTrace();
			System.exit(-1);
		}
	}

	public void sendRes(String res){
		System.out.println("Sending Results");
		try{
		PrintWriter pw = new PrintWriter(clientSocket.getOutputStream(),true);
		pw.println(res);
		}catch(Exception e){
			System.out.println("SendRes -- Exception "+e);
			System.exit(-1);
		}
		
	}
}

class Process
{
    public long arrival;
	public String clientID;
    public int burst,burst_remainning;
    public int priority;
    public int processNumber;
    public long turnAround;
    public long waitingTime;

    public String toString()
    {
        return "ProcessNumber = "+processNumber+"\n"+
			   "Burst = "+burst+"\n"+
			   "Priority = "+priority+"\n"+
			   "Turn Around Time = "+(turnAround/1000)+" sec\n"+
			   "Waiting Time = "+(waitingTime/1000)+" sec\n";
    }

}

class Server{
	
	public static int totalClientConnected = -1;
	public static ConnectionHandler[] connection = new ConnectionHandler[50];

	public static Thread executeThread = null;

	public static PriorityQueue<Process> processQueue = new PriorityQueue<Process>(20, new Comparator<Process>(){
		public int compare(Process p1, Process p2){
			return p1.priority - p2.priority;
		}
	});


	public static void addNewProcess(Process p){
		synchronized(processQueue){
			processQueue.add(p);
		}
	}

	public static Process getPeekProcesses(){
		synchronized(processQueue){
			return processQueue.peek();
		}
	}

	public static void removeProcess(Process process){
		synchronized(processQueue){						
			processQueue.remove(process);
		}
	}


	public static void main(String args[]){
		try{
			ServerSocket serverSocket = new ServerSocket(4444);
			System.out.println("Server has Started");
			startExecuteThread();
			while(true){
				Socket clientSocket = serverSocket.accept();
								
				totalClientConnected++;
				connection[totalClientConnected] = new ConnectionHandler(clientSocket,totalClientConnected+"");
					

				new Thread(connection[totalClientConnected]).start();			
			}		
		}catch(IOException e){
			System.out.println("Exception "+e);
			System.exit(-1);
		}
	}

	public static synchronized void executeProcess(String req){
		
		//Request String has the format :-  clientID processNumber burst priority
		
		//processNumber will be like clientID+processCount       processCount->The process count of that client
		String[] request = req.split(" ");

		Process newProcess = new Process();

		newProcess.clientID = request[0];
		newProcess.processNumber = Integer.parseInt(request[1]);
		newProcess.burst=newProcess.burst_remainning=Integer.parseInt(request[2]);
        newProcess.priority=Integer.parseInt(request[3]);
		newProcess.arrival=System.currentTimeMillis();		

		//process.add(newProcess);
		
		addNewProcess(newProcess);


		//getProcesses(1,newProcess);
		System.out.println("\tADDING PROCESS NUMBER "+newProcess.processNumber+" TO THE SERVER AT "+newProcess.arrival+"ms");
		System.out.println();
	}


	public static void startExecuteThread(){
		
		executeThread = new Thread(new Runnable(){
		
			public void run(){
				Process scheduledProcess = null;

        		while(true)
        		{
					scheduledProcess = getPeekProcesses();
					
					if(scheduledProcess == null){
						continue;
					}

            		scheduledProcess.burst_remainning--;

					try{
						Thread.sleep(1000);
					} catch(Exception e){
						e.printStackTrace();
					}

            		System.out.println("At Time = "+(System.currentTimeMillis())+" -- Process Selected = "+scheduledProcess.processNumber);
            		if(scheduledProcess.burst_remainning==0)
            		{

						//Calculate Turn Around Time as ( Current System Time - Process Arrival Time )
            		    scheduledProcess.turnAround = System.currentTimeMillis() - scheduledProcess.arrival;
						//Calculate Waiting Time as ( Turn Around Time - Process Burst Time )
                		scheduledProcess.waitingTime = scheduledProcess.turnAround-scheduledProcess.burst*1000;
						
						//Notify the Client that requested the process.
						connection[Integer.parseInt(scheduledProcess.clientID)].sendRes(scheduledProcess.toString());

						//Remove the process from the Server.
						removeProcess(scheduledProcess);
						
						System.out.println("\tProcess -- "+scheduledProcess.processNumber+" has Completed");
            		}
        		}
				
			}

		},"execute");
		executeThread.start();
	}
}
