package irc;

import java.awt.*;
import java.awt.event.*; 


import jvn.*;
import java.io.*;


public class IrcTestStress {
	public TextArea		text;
	public TextField	data;
	Frame 			frame;
	SentenceProxy       sentence;

	int Max_thread = 5;
	int Max_loop = 50;

    public IrcTestStress(SentenceProxy jo) {
        sentence = jo;
    }

    /**
    * main method
    * create a JVN object nammed IRC for representing the Chat application
    **/
    public static void main(String argv[]) {
        IrcTestStress irc = null;
        try {
            // create the graphical part of the Chat application
            SentenceProxy jo = (SentenceProxy) JvnProxy.newInstance(new Sentence(),"IRC");
            irc = new IrcTestStress(jo);
        } catch (Exception e) {
            System.out.println("IRC problem : " + e.getMessage());
        }
        irc.stressTest();
    }


    public void stressTest(){
        for (int i = 0; i< Max_thread; i++){

        Thread thread = new Thread(){
            @Override
            public void run() {
                long threadId = Thread.currentThread().getId();

                int Max = 50;
                for (int i=0;i< Max_loop ;i++){
                    write("I =" +i);
                    read(threadId);
                }
            }
        };
            thread.run();
        }

    }

    public void read(long threadId){
        System.out.println("-----------------  Read in thread "+threadId+":");
        Object res = this.sentence.read();
        System.out.println("Read result            :"+res);
    }


    public void write(String s){
        this.sentence.write(s);
        System.out.println("-----------------  Write Done!");
    }
}
