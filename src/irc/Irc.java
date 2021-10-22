/***
 * Irc class : simple implementation of a chat using JAVANAISE 
 * Contact: 
 *
 * Authors: 
 */

package irc;

import java.awt.*;
import java.awt.event.*; 


import jvn.*;
import java.io.*;


public class Irc {
	public TextArea		text;
	public TextField	data;
	Frame 			frame;
	JvnObject       sentence;


  /**
  * main method
  * create a JVN object nammed IRC for representing the Chat application
  **/
	public static void main(String argv[]) {
	   try {
		   
		// initialize JVN
		JvnServerImpl js = JvnServerImpl.jvnGetServer();

		   // look up the IRC object in the JVN server
		// if not found, create it, and register it in the JVN server
		JvnObject jo = js.jvnLookupObject("IRC");
		if (jo == null) {

			jo = js.jvnCreateObject((Serializable) new Sentence());

			// after creation, I have a write lock on the object
			jo.jvnUnLock();

			js.jvnRegisterObject("IRC", jo);
		}
		// create the graphical part of the Chat application
		 new Irc(jo);
	   } catch (Exception e) {
		   System.out.println("IRC problem : " + e.getMessage());
	   }
	}

  /**
   * IRC Constructor
   @param jo the JVN object representing the Chat
   **/
	public Irc(JvnObject jo) {
		sentence = jo;
		frame=new Frame();
		frame.setLayout(new GridLayout(1,1));
		text=new TextArea(10,60);
		text.setEditable(false);
		text.setForeground(Color.red);
		frame.add(text);
		data=new TextField(40);
		frame.add(data);
		Button read_button = new Button("read");
		read_button.addActionListener(new readListener(this));
		frame.add(read_button);
		Button write_button = new Button("write");
		write_button.addActionListener(new writeListener(this));
		frame.add(write_button);
		frame.setSize(545,201);
		text.setBackground(Color.black); 
		frame.setVisible(true);
	}
}


 /**
  * Internal class to manage user events (read) on the CHAT application
  **/
 class readListener implements ActionListener {
	Irc irc;
  
	public readListener (Irc i) {
		irc = i;
	}
   
 /**
  * Management of user events
  **/
	public void actionPerformed (ActionEvent e) {
	 try {
			System.out.println("--------------------------------------------");
			System.out.println("Lets Read!");
			// lock the object in read mode
			irc.sentence.jvnLockRead();
			System.out.println("jvn Lock Read DONE!");

			// invoke the method
			String s = ((Sentence)(irc.sentence.jvnGetSharedObject())).read();
			System.out.println("jvn Lock Read DONE in OBJ!");

			// unlock the object
			irc.sentence.jvnUnLock();
			System.out.println("jvn UnLock READ DONE!");

			// display the read value
			irc.data.setText(s);
			irc.text.append(s+"\n");
	   } catch (JvnException | InterruptedException je) {
		   System.out.println("IRC problem : " + je.getMessage());
	   }
	}
}

 /**
  * Internal class to manage user events (write) on the CHAT application
  **/
 class writeListener implements ActionListener {
	Irc irc;
  
	public writeListener (Irc i) {
        	irc = i;
	}
  
  /**
    * Management of user events
   **/
	public void actionPerformed (ActionEvent e) {
		try {
			System.out.println("--------------------------------------------");
			// get the value to be written from the buffer
			String s = irc.data.getText();
			System.out.println("Lets Write : " + s);
			// lock the object in write mode
			irc.sentence.jvnLockWrite();
			System.out.println("jvn Lock Write DONE!");
			// invoke the method
			((Sentence)(irc.sentence.jvnGetSharedObject())).write(s);
			System.out.println("jvn Lock Write DONE in OBJ!");

			// unlock the object
			irc.sentence.jvnUnLock();
			System.out.println("jvn UnLock Write DONE!");

	 } catch (JvnException | InterruptedException je) {
		   System.out.println("IRC problem  : " + je.getMessage());
	 }
	}

	 @Override
	 public String toString() {
		 return "writeListener{" +
				 "irc=" + irc +
				 '}';
	 }
 }



