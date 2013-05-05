import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;
import java.text.*;


class MySemaphores
{
	private static Semaphore semMutex = new Semaphore (1,true);	// semaphore for mutual exclusion of resource

	public static void hashMapWait() throws InterruptedException
	{
		semMutex.acquire();
	}

	public static void hashMapSignal()
	{
		semMutex.release();
	}
}

class dataStructures
{
	public static HashMap<String,Messages[]> info = new HashMap<String,Messages[]>();	//hashmap for storing messages
	public static HashMap<String,Integer> allusers = new HashMap<String, Integer> ();	//hashmap for all known users and storing the current value of message pointer
	public static HashMap<String,Integer> onlineStatus = new HashMap<String,Integer> (); //hashmap for all online users
}

// creating a class which stores the message and its attributes
class Messages
{
	String sender;
	String time;
	String message;
}

class ClientWorker implements Runnable 
{
	private Socket client;
	public String name;
	Messages msg[];
	
	
	ClientWorker(Socket client) 
	{
		this.client = client;
	}

	// defining the message attributes
	public void createMsg()
	{
		msg = new Messages[10];

		for (int i=0; i<=9; i++)
		{
			msg[i] = new Messages();
			msg[i].sender = new String();
			msg[i].time = new String();
			msg[i].message = new String();
		}
	}

	public void run()
	{
		int choice;
		BufferedReader in = null;
		PrintWriter out = null;
		String ack = "1";
		String user2;
		String userMessage;
		String userDate;
		String sender;
		String myMessage;

		DateFormat dateFormat = new SimpleDateFormat("MM/dd/yy hh:mm a");

		int messagePointer = 0;


		try 
		{
			in = new BufferedReader(new InputStreamReader(client.getInputStream()));
			out = new PrintWriter(client.getOutputStream(), true);
		} 
		catch (IOException e) 
		{
			System.out.println("in or out failed");
			System.exit(-1);
		}

		try 
		{
			// Receive text from client
			name = in.readLine();

			// check whether the incoming client is already online or not
			if (dataStructures.onlineStatus.containsKey(name))	
			{
				out.println("-1");
				try 
				{
					client.close();
					return;
				} 
				catch (IOException e) 
				{
					System.out.println("Close failed");
					System.exit(-1);
				}
			}
			out.println(ack);
			
			// if connection is by known user, set variables like previous message pointers and pointing to previous message array.
			if (dataStructures.allusers.containsKey(name))
			{
				messagePointer = dataStructures.allusers.get(name);
				msg = dataStructures.info.get(name);
				System.out.println(dateFormat.format(Calendar.getInstance().getTime()) + " Connection by known user " + name);
			}
			else
			{
				createMsg();
				System.out.println(dateFormat.format(Calendar.getInstance().getTime()) + " Connection by unknown user " + name);
			}

			
			try
			{
				//setting the hashmaps

				MySemaphores.hashMapWait();
					dataStructures.onlineStatus.put(name,1);
					dataStructures.info.put(name, msg);
					dataStructures.allusers.put(name,messagePointer);
				MySemaphores.hashMapSignal();
			}
			catch (InterruptedException e)
			{
			}

			do
			{
				
				choice = Integer.parseInt(in.readLine());
				//System.out.println(choice);
				switch (choice)
				{
					case 1:
							// display all known users
							
							try
							{
								MySemaphores.hashMapWait();
								
								int i = 1;
								String users = "";
								for (Map.Entry<String,Integer> entry : dataStructures.allusers.entrySet())
								{
									users = "\t\t" + i + ".  " + entry.getKey();
									out.println(users);
									ack = in.readLine();
									i++;
								}
								out.println("-1");

								
								MySemaphores.hashMapSignal();
							}
							catch (InterruptedException e)
							{
							}

							System.out.println(dateFormat.format(Calendar.getInstance().getTime()) + " " + name + " displays all known users.");

							break;
					case 2: // displaying names of all currently connected users
							
							try
							{
								MySemaphores.hashMapWait();
								
								int i = 1;
								String users = "";
								for (Map.Entry<String,Integer> entry : dataStructures.onlineStatus.entrySet())
								{
									users = "\t\t" + i + ".  " + entry.getKey();
									out.println(users);
									ack = in.readLine();
									i++;
								}
								out.println("-1");

								
								MySemaphores.hashMapSignal();

							}
							catch (InterruptedException e)
							{
							}

							System.out.println(dateFormat.format(Calendar.getInstance().getTime()) + " " + name + " displays all currently connected users.");
							
							break;
					case 3: // send text message to particular user
							
							out.println("\tEnter recepient's name: ");
							user2 = in.readLine();
							
							// checking if the user is a known user or not. if not, then not allowing the send message
							if (dataStructures.allusers.containsKey(user2))
							{
								out.println("\tEnter a message: ");
								userMessage = in.readLine();

								out.println(ack);
								userDate = in.readLine();

								out.println(ack);
								sender = in.readLine();
								
								msg = dataStructures.info.get(user2);					// pointing to existing message array
								messagePointer = dataStructures.allusers.get(user2);	// getting the previous message pointer
								msg[messagePointer].sender = sender;
								msg[messagePointer].time = userDate;
								msg[messagePointer].message = userMessage;

								try
								{
									MySemaphores.hashMapWait();
									
									dataStructures.info.put(user2, msg);				//updating the message hashmap
									messagePointer++;									
									dataStructures.allusers.put(user2,messagePointer);	// updating the message pointer
									MySemaphores.hashMapSignal();
								}
								catch (InterruptedException e)
								{
								}

								System.out.println(dateFormat.format(Calendar.getInstance().getTime()) + " " + name + " posts message for " + user2);

							}
							else
							{
								out.println("-1");
							}

							break;

					case 4: // send message to all currently connected users
						
							out.println("\tEnter a message: ");
							userMessage = in.readLine();

							out.println(ack);
							userDate = in.readLine();

							out.println(ack);
							sender = in.readLine();

							try
							{
								MySemaphores.hashMapWait();
								
								for (Map.Entry<String,Messages[]> entry : dataStructures.info.entrySet())
								{

									String currentUser = entry.getKey();

									if (! currentUser.equals(name) && dataStructures.onlineStatus.containsKey(currentUser))
									{
										

										msg = dataStructures.info.get(currentUser);
										messagePointer = (int) dataStructures.allusers.get(currentUser);

										msg[messagePointer].sender = sender;
										msg[messagePointer].time = userDate;
										msg[messagePointer].message = userMessage;
										
											
										dataStructures.info.put(currentUser, msg);
										messagePointer++;				
										dataStructures.allusers.put(currentUser,messagePointer);
									}
								}

								MySemaphores.hashMapSignal();

							}
							catch (InterruptedException e)
							{
							}

							System.out.println(dateFormat.format(Calendar.getInstance().getTime()) + " " + name + " posts message for all known users.");

							break;

					case 5: // send message to all currently connected users
						
							out.println("\tEnter a message: ");
							userMessage = in.readLine();

							out.println(ack);
							userDate = in.readLine();

							out.println(ack);
							sender = in.readLine();

							try
							{
								MySemaphores.hashMapWait();
								
								for (Map.Entry<String,Messages[]> entry : dataStructures.info.entrySet())
								{

									String currentUser = entry.getKey();

									if (! currentUser.equals(name))
									{
										

										msg = dataStructures.info.get(currentUser);
										messagePointer = (int) dataStructures.allusers.get(currentUser);

										msg[messagePointer].sender = sender;
										msg[messagePointer].time = userDate;
										msg[messagePointer].message = userMessage;
										
											
										dataStructures.info.put(currentUser, msg);
										messagePointer++;				
										dataStructures.allusers.put(currentUser,messagePointer);
									}
								}

								MySemaphores.hashMapSignal();

							}
							catch (InterruptedException e)
							{
							}
							System.out.println(dateFormat.format(Calendar.getInstance().getTime()) + " " + name + " posts message for all currently connected users.");
							break;
					
					case 6:
							// get my messages
							try
							{
								if ((int) dataStructures.allusers.get(name) == 0)
								{
									out.println("No new message");
									ack = in.readLine();
									out.println("-1");
									System.out.println(dateFormat.format(Calendar.getInstance().getTime()) + " " + name + " gets messages.");
									break;
								}
								
								MySemaphores.hashMapWait();
								Messages m[] = dataStructures.info.get(name);
								
								messagePointer = dataStructures.allusers.get(name);
								
								for (int i=0; i<messagePointer ; i++)
								{
									myMessage ="\t" + (i+1) + "\tFrom " + m[i].sender + ", " + m[i].time + ", " + m[i].message;
									out.println(myMessage);
									ack = in.readLine();
								}
								
								out.println("-1");

								MySemaphores.hashMapSignal();
							}
							catch (InterruptedException e)
							{
							}

							dataStructures.allusers.put(name,0);
							System.out.println(dateFormat.format(Calendar.getInstance().getTime()) + " " + name + " gets messages.");
							break;
					case 7:
							dataStructures.onlineStatus.remove(name);
							try 
							{
								client.close();
							} 
							catch (IOException e) 
							{
								System.out.println("Close failed");
								System.exit(-1);
							}

							System.out.println(dateFormat.format(Calendar.getInstance().getTime()) + " " + name + " exits.");
							break;
				}
			}
			while (choice != 7);
		} 
		catch (IOException e) 
		{
			System.out.println("Read failed");
			System.exit(-1);
		}

	}
}

class SocketThrdServer 
{
	ServerSocket server = null;

	public void listenSocket(int port)
	{
		try
		{
			server = new ServerSocket(port); 
			System.out.println("Server running on port " + port + 
							 "," + " use ctrl-C to end");
		} 
		catch (IOException e) 
		{
			System.out.println("Error creating socket");
			System.exit(-1);
		}
		
		ClientWorker w[] = new ClientWorker[100];
		int i=0;
		while(true)
		{
			if(i> 99)
				break;

			try
			{
				w[i] = new ClientWorker(server.accept());
				Thread t = new Thread(w[i]);
				t.start();
			} 
			catch (IOException e) 
			{
				System.out.println("Accept failed");
				System.exit(-1);
			}
			i++;
		}
	}

	protected void finalize()
	{
		try
		{
			server.close();
		} 
		catch (IOException e) 
		{
			System.out.println("Could not close socket");
			System.exit(-1);
		}
	}

	public static void main(String[] args)
	{
		if (args.length != 1)
		{
			System.out.println("Usage: java SocketThrdServer port");
			System.exit(1);
		}

		SocketThrdServer server = new SocketThrdServer();
		int port = Integer.valueOf(args[0]);
		server.listenSocket(port);
	}
}