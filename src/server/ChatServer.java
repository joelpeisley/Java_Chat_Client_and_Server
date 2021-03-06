package server;
import java.io.*;		//Needed for general input and output classes
import java.net.*;		//Needed for the DatagramSocket and DatagramClient classes
import java.util.*;		//For the use of the Vector object

import javax.swing.JOptionPane;

/**********************************************************************
* ChatServer
* This is the ChatServer program that forms the part of the distributed
* application that is responsible for receiving the data from the
* individual clients and distributing it to all other clients that
* are registered, adding new clients to a database who wish to join in the 
* session and removing clients from the client database if they wish 
* to leave.
***********************************************************************/
public class ChatServer{

	private static Vector<ClientEntry> clients;
	private static int server_port;
	private static DatagramSocket serverSocket = null;
	
	/**
	 * 
	 * @param args Should contain the server port to operate on.
	 */
	public static void main(String args[]){
	
		//Get the port from the command line arguments -> Read into server_port
		if(args.length != 1){
		        System.out.println("Usage: ChatServer <port>");
		        System.exit(-1);
		}else {
		        server_port = Integer.parseInt(args[0]);
		}
			
		//Allocate an initial block of memory to store the client list (Vector will automatically increase the size if need be)
		clients = new Vector<ClientEntry>(100);
	        
		try{
			//Create a DatagramSocket object to listen on server_port
			serverSocket = new DatagramSocket(server_port);
		}
		catch(SocketException e){
			JOptionPane.showMessageDialog(null, "SocketExecption!, This may be caused by the port being taken or not having rights to bind to ports.");
			System.exit(-1);
		}catch(Exception e){
			JOptionPane.showMessageDialog(null, "General Exception!\n"+e);
			System.exit(-1);
		}

		byte[] receiveData = new byte[1024];
		DatagramPacket receivePacket = null;
		System.out.println("Chat server started on port "+server_port+". Press [ctrl+C] to stop.");
		//Loop Forever
		for( ; ; ){
			try{
				//Wait to receive a datagram on the DatagramSocket
				receivePacket = new DatagramPacket(receiveData, receiveData.length);
				serverSocket.receive(receivePacket);
				receiveData = new byte[1024];
			}
			//Catches a possible IOException and exits as error if this occurs
			catch(Exception ex){
				JOptionPane.showMessageDialog(null, "General Exception!\n"+ex);
				System.exit(-1);
			}
			
			//retrieve the datagrams contents
			byte[] b1 = null;
			String content = null;
			try{
			        b1 = receivePacket.getData();
			        content = new String(b1, 0, b1.length, "UTF-8");
			}catch (UnsupportedEncodingException e){
			        JOptionPane.showMessageDialog(null, "Unsupported Encoding Exception!\n"+e);
			        System.exit(-1);
			}

			if(content.contains("\\disconnect")){
				//Remove the client entry from clients list (if found)
				//Notify all other clients of its disconnection (if sending client was actually in list)
				int k = findClient(receivePacket.getPort(), receivePacket.getAddress());
				if(k != -1){
				        clients.remove(k);
				        try {
				        	broadcastMessage(InetAddress.getByName("localhost")+":"+server_port+
				                ">"+receivePacket.getAddress().toString()+":"+
				                receivePacket.getPort()+" DISCONNECTED!");
		                } catch (Exception e){
		                        JOptionPane.showMessageDialog(null, "Disconnect Error!"+e);
		                        System.exit(-1);
		                }
		        }
			} else if(content.contains("\\join")){
				//Check if client is currently in clients list (by IP address and client side Port)
				//Add new client if not present
				//Generate output to be sent to all clients (i.e. IP address as the clients identifier)
				if(findClient(receivePacket.getPort(), receivePacket.getAddress()) == -1){
				        clients.add(new ClientEntry(receivePacket.getPort(), receivePacket.getAddress()));
				}
				broadcastMessage(receivePacket.getAddress().toString()+":"+
				        receivePacket.getPort()+" CONNECTED!");
			
			} else {
			
				//Check if client is currently in clients list (by IP addr. and client side Port)
				//Add new client if not present(this allows clients to still join the chat server if their initial \join packet was lost)               
				if(findClient(receivePacket.getPort(), receivePacket.getAddress()) == -1){
				        clients.add(new ClientEntry(receivePacket.getPort(), receivePacket.getAddress()));
				        //send a join notification if a new client is added
				        broadcastMessage(receivePacket.getAddress().toString()+":"+
				        receivePacket.getPort()+" CONNECTED!");
				}		
				//Generate output to be sent to all clients (i.e. IP address as the clients identifier)
				//Send datagram contents to all clients
				broadcastMessage(receivePacket.getAddress().toString()+":"+
				        receivePacket.getPort()+">"+content);
			}
		
		}//for loop
	
	
	}//end main
	/**
	* This method will broadcast the string as a message to all clients in the list.
	*
	* @param s The message as a String.
	**/
	private static void broadcastMessage(String s){
		try{
	        for(int j = 0; j < clients.size(); j++){
	                DatagramPacket send = new DatagramPacket(s.getBytes("UTF-8"),
	                        s.getBytes("UTF-8").length, clients.get(j).getAdr(), clients.get(j).getPort());
                    serverSocket.send(send);
	        }
	    } catch (Exception er) {
	    	JOptionPane.showMessageDialog(null, "Error!\n"+er);
	    	System.exit(-1);
	    }
	}
	/**
	* This will check if a client is in the list
	*
	* @param i The port to search for
	* @param a the InetAddress to search for
	* @return -1 if not found, else returns the position of the found client.
	**/
	private static int findClient(int i, InetAddress a){
		for(int j = 0; j < clients.size(); j++){
			if((clients.get(j).getAdr().toString().equals(a.toString())) && (clients.get(j).getPort() == i)){
				return j;
	        }
		}
		return -1;
	}
}//end class