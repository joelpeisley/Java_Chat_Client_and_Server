package client;
import java.net.*;		//For the datagramSocket and DatagramClient 

import javax.swing.JOptionPane;
import javax.swing.text.*;
import java.util.*;

/*******************************************************************************
* Class ChatListener 
* The purpose of this class is to listen on the ChatClients open port for 
* incoming datagrams and display their content to the output object on 
* the ChatClient interface. This is done in a second thread of execution
* (i.e. concurrently to the ChatClients main thread of execution.)
* 
* To Achieve this, the class implements the Runnable interface and the required 
* method run() which contains the operations that occur in the concurrent thread
*******************************************************************************/
public class ChatListener implements Runnable{
	
	ChatClient current;				//Creates a reference to a ChatClient object
	DatagramSocket in;				//Creates a reference to a datagramSocket object
	Thread t;						//Creates a ref to a thread object
	public static int BUFFER_SIZE = 1024; 		//Declares an int to the specify the Buffer size
	byte[] curbuffer= new byte[BUFFER_SIZE];	//Declares a byte array to store the content of the curent packet
	
	/************************************************************************************
	* The function ChatListener(DatagramSocket x, ChatClient y) is the constructor
	* of the chat listener class and simply performs some basic assignments and
	* starts the new thread of execution that this class object operates in. This is
	* done with the thread object which is created with this object passed as a parameter
	* and then this objects start() method is called.
	*
	* @param x The Datagram socket to use
	* @param y The ChatClient to use
	************************************************************************************/
	ChatListener(DatagramSocket x, ChatClient y){
		in=x;									//Assigns the DatagramSocket passed to the constructor to in
		current=y;								//Assigns the ChatClient passed to the constructor t current
		t = new Thread(this, "ChatListener");	//Creates a new thread object (i.e. a ne thraed of execution
												//and adds this object to this thread so its executes concurrently
		t.start();								//Starts the new thread of execution
	}
	/************************************************************************************
	* This is the run() function that is implemented from the runnable interface.
	* It contains the code that is executed in the concurrent thread.
	*
	* Not that the receive() method of the datagramSocket class causes this thread to block
	* until a datagram is received so that the program is not continually polling the port:
	* any polling that occurs is done in the background with implementation of the method
	*	
	************************************************************************************/
	public void run(){

		try{
			//Declare a DatagramPacket object current and constructs a DatagramPacket object
			DatagramPacket receivePacket = new DatagramPacket(curbuffer, curbuffer.length);
		        
			//This is the forever loop that is responsible for receiving Datagrams from the
			//Server socket and displaying them on the output object of ChatClient interface
			for( ; ; ){
			
				//Receive the datagrams from the server an display to the output panel
				in.receive(receivePacket);
		                try{
		                        Document doc = current.output.getDocument();
		                        String mess = new String(receivePacket.getData(), "UTF-8");
		                        StringTokenizer st = new StringTokenizer(mess, "!", true);
		                        if(mess.contains("DISCONNECTED!") && mess.contains("CONNECTED!") && st.countTokens() == 5){
		                                doc.insertString(doc.getLength(), "\n"+st.nextToken()+"!", null);
		                        }else {
		                                doc.insertString(doc.getLength(), "\n"+mess, null);
		                        }
		                }catch (Exception e) {
		                	JOptionPane.showMessageDialog(null, "Error!\n"+e);
		                }
				curbuffer = null;
				curbuffer = new byte[BUFFER_SIZE];
			}
		}
		catch(SocketException e){
			JOptionPane.showMessageDialog(null,"Socket Exception!\n"+e);
		}	
		catch(Exception e){
			JOptionPane.showMessageDialog(null, "General Exception!\n"+e);
		} 
	}
	/***********************************************************************************
	* This is the finalize() method which is called when the object is destroyed. It
	* contains any finalization clean up code that must be carried out. In this 
	* situation the only operation that is done is that the dataGramSocket is closed
	* so that the port can be used by another program.
	*
	***********************************************************************************/
	protected void finalize(){
		in.close();	//Closes the connection
	}
}