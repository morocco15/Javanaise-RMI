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


public class IrcProxy {
	public TextArea		text;
	public TextField	data;
	Frame 			frame;
	SentenceProxy       sentence;


  /**
  * main method
  * create a JVN object nammed IRC for representing the Chat application
  **/
	public static void main(String argv[]) {
	   try {

		SentenceProxy jo = (SentenceProxy) JvnProxy.newInstance(new Sentence(),"IRC");
		// create the graphical part of the Chat application
		 new IrcProxy(jo);

	   } catch (Exception e) {
		   System.out.println("IRC problem : " + e.getMessage());
	   }
	}

  /**
   * IRC Constructor
   @param jo the JVN object representing the Chat
   **/
	public IrcProxy(SentenceProxy jo) {
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
		read_button.addActionListener(new readListenerProxy(this));
		frame.add(read_button);
		Button write_button = new Button("write");
		write_button.addActionListener(new writeListenerProxy(this));
		frame.add(write_button);
		frame.setSize(545,201);
		text.setBackground(Color.black); 
		frame.setVisible(true);
	}
}


 /**
  * Internal class to manage user events (read) on the CHAT application
  **/
 class readListenerProxy implements ActionListener {
	 IrcProxy irc;
  
	public readListenerProxy (IrcProxy i) {
		irc = i;
	}
   
 /**
  * Management of user events
  **/
	public void actionPerformed (ActionEvent e) {
		System.out.println("--------------------------------------------");
		System.out.println("Lets Read!");
		Object res = irc.sentence.read();
		System.out.println("res  :"+res);

		if(res instanceof String) {
			String s = (String) res;
			// display the read value
			irc.data.setText(s);
			irc.text.append(s+"\n");
		}

	}
}

 /**
  * Internal class to manage user events (write) on the CHAT application
  **/
 class writeListenerProxy implements ActionListener {
	 IrcProxy irc;
  
	public writeListenerProxy (IrcProxy i) {
        	irc = i;
	}
  
  /**
    * Management of user events
   **/
	public void actionPerformed (ActionEvent e) {
		System.out.println("--------------------------------------------");
		System.out.println("Lets Write!");

		// get the value to be written from the buffer
		String s = irc.data.getText();
		irc.sentence.write(s);
	}

	 @Override
	 public String toString() {
		 return "writeListener{" +
				 "irc=" + irc +
				 '}';
	 }
 }



