import java.io.*;
import java.util.*;
import java.net.*;
import java.text.*;

public class SocketClient
{
	Socket socket = null;
	PrintWriter out = null;
	BufferedReader in = null;

	public void printMenu()
	{
		System.out.println("\n");

		System.out.println("\t1.  Display the names of all known users");
		System.out.println("\t2.  Display names of all currently connected users");
		System.out.println("\t3.  Send a text message to a particular user.");
		System.out.println("\t4.  Send a text message to all currently connected users");
		System.out.println("\t5.  Send a text message to all known users");
		System.out.println("\t6.  Get my messages");
		System.out.println("\t7.  Exit\n");
		
		System.out.print("Enter your Choice: ");
	}
   
	public void communicate() throws IOException
	{
		Scanner sc = new Scanner(System.in);
		System.out.println("Enter your name: ");
		String name = sc.nextLine();
		String users;
		String reply;
		String user2;
		String message;
		String nowDateTime;
		String myMessage;
		DateFormat dateFormat = new SimpleDateFormat("MM/dd/yy hh:mm a");
		Calendar cal;

		//Send data over socket
		out.println(name);
		try
		{
			//Receive text from server
			reply = in.readLine();

			if("-1".equals(reply))
			{
				System.out.println("User already Connected");
				return;
			}

		} 
		catch (IOException e)
		{
			System.out.println("Read failed");
			System.exit(1);
		}


		int choice;
		String ack = "1";
		do
		{
			printMenu();		// function to print menu

			try
			{
				choice = Integer.parseInt(sc.nextLine());
			}
			catch (Exception e)
			{
				System.out.println("Please enter a digit");
				choice = -1;
				continue;
			}
			
			
			switch (choice)
			{
				case 1: // Display the names of all known users
							out.println(choice);
							users = in.readLine();
							
							while(!"-1".equals(users))
							{
								System.out.println(users);
								out.println(ack);
								users = in.readLine();
							}
							
							break;

				case 2:	// displya names of all currently connected users

							out.println(choice);
							users = in.readLine();
							
							while(!"-1".equals(users))
							{
								System.out.println(users);
								out.println(ack);
								users = in.readLine();
							}
							
							break;
				case 3:	// send a text message to a particular user.
							out.println(choice);
							
							reply = in.readLine();				
							System.out.print(reply);		// server asking for name of second user

							user2 = sc.nextLine();			// keyboard input for second user
							out.println(user2);

							reply = in.readLine();			// getting server response
							
							if ("-1".equals(reply))			// checking whether the second user is present or not. if not, break this case
							{
								System.out.println("\t\tClient doesn't exist.");
								break;
							}
							
							System.out.print(reply);

							message = sc.nextLine();
							out.println(message);		// input the message

							

							reply = in.readLine();
							cal = Calendar.getInstance();
							nowDateTime = dateFormat.format(cal.getTime());		//set calendar and date
							out.println(nowDateTime);					// send the dates
							
							reply = in.readLine();
							out.println(name);

							break;
				case 4:
							// send message to all currently connected users
							
							out.println(choice);
							reply = in.readLine();	
							
							System.out.print(reply);
							message = sc.nextLine();
							out.println(message);

							reply = in.readLine();
							cal = Calendar.getInstance();
							nowDateTime = dateFormat.format(cal.getTime());
							out.println(nowDateTime);
							
							reply = in.readLine();
							out.println(name);

							break;
				case 5:
							// send message to all known users
							
							out.println(choice);
							reply = in.readLine();
							
							System.out.print(reply);
							message = sc.nextLine();
							out.println(message);

							reply = in.readLine();
							cal = Calendar.getInstance();
							nowDateTime = dateFormat.format(cal.getTime());
							out.println(nowDateTime);
							
							reply = in.readLine();
							out.println(name);
							break;
				case 6:		// get my messages

							out.println(choice);
							myMessage = in.readLine();

							while(!"-1".equals(myMessage))
							{
								System.out.println(myMessage);
								out.println(ack);
								myMessage = in.readLine();
							}

							break;
				case 7:
							out.println(choice);
							break;

				default:
							System.out.println ("Invalid Input, Please try again.\n");
			}
		}while(choice != 7);

 //one finally block

		
		
	}
  
	public void listenSocket(String host, int port)
	{
		//Create socket connection
		try
		{
			socket = new Socket(host, port);
			out = new PrintWriter(socket.getOutputStream(), true);
			in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		} 
		catch (UnknownHostException e) 
		{
			System.out.println("Unknown host");
			System.exit(1);
		} 
		catch (IOException e) 
		{
			System.out.println("No I/O");
			System.exit(1);
		}
	}

	public static void main(String[] args) throws IOException
	{
		if (args.length != 2)
		{
			System.out.println("Usage:  client hostname port");
			System.exit(1);
		}

		SocketClient client = new SocketClient();

		String host = args[0];
		int port = Integer.valueOf(args[1]);
		client.listenSocket(host, port);
		client.communicate();
	}
}