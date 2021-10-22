/***
 * JAVANAISE Implementation
 * JvnServerImpl class
 * Implementation of a Jvn server
 * Contact:
 *
 * Authors:
 * 		Walid BOUKRIS
 * 	    Labib Bouri
 */

package jvn;

import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;
import java.io.*;



public class JvnServerImpl 	
              extends UnicastRemoteObject 
							implements JvnLocalServer, JvnRemoteServer{ 
	
  /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	// A JVN server is managed as a singleton 
	private static JvnServerImpl js = null;

	private JvnRemoteCoord coordinator;

	// Local Map of JVN objects
	private HashMap<Integer, JvnObject> hashmapObjects;

  /**
  * Default constructor
  * @throws JvnException
  **/
	private JvnServerImpl() throws Exception {
		super();
		this.hashmapObjects = new HashMap<Integer, JvnObject>();

		try {
			Registry registry = LocateRegistry.getRegistry(4000);
			coordinator = (JvnRemoteCoord) registry.lookup("coordinator");
		} catch (Exception e) {
			System.err.println("Jvn Coordinator exception : " + e.getMessage());
		}
	}
	
  /**
    * Static method allowing an application to get a reference to 
    * a JVN server instance
    * @throws JvnException
    **/
	public static JvnServerImpl jvnGetServer() {
		if (js == null){
			try {
				js = new JvnServerImpl();
			} catch (Exception e) {
				System.err.println("Jvn Server exception : " + e.getMessage());
				return null;
			}
		}
		return js;
	}
	
	/**
	* The JVN service is not used anymore
	* @throws JvnException
	**/
	public  void jvnTerminate()
	throws jvn.JvnException {
		try{
			coordinator.jvnTerminate(this);
		}catch(Exception e){
			JvnException err = new JvnException(e.getMessage());
			throw(err);
		}
	} 
	
	/**
	* creation of a JVN object
	* @param o : the JVN object state
	* @throws JvnException
	**/
	public  JvnObject jvnCreateObject(Serializable o)
	throws jvn.JvnException {
		try{
			int id = coordinator.jvnGetObjectId();
			JvnObject obj = new JvnObjectImpl(id, o);
			return obj;
		}catch(Exception e){
			JvnException err = new JvnException(e.getMessage());
			throw(err);
		}
	}
	
	/**
	*  Associate a symbolic name with a JVN object
	* @param jon : the JVN object name
	* @param jo : the JVN object 
	* @throws JvnException
	**/
	public  void jvnRegisterObject(String jon, JvnObject jo)
	throws jvn.JvnException {
		try{
			coordinator.jvnRegisterObject(jon,jo,this);
			//Put object in LOCAL!
			hashmapObjects.put(jo.jvnGetObjectId(), jo);
		}catch(Exception e){
			JvnException err = new JvnException(e.getMessage());
			throw(err);
		}
	}
	
	/**
	* Provide the reference of a JVN object beeing given its symbolic name
	* @param jon : the JVN object name
	* @return the JVN object 
	* @throws JvnException
	**/
	public  JvnObject jvnLookupObject(String jon)
	throws jvn.JvnException {

		try{
			JvnObject obj = this.coordinator.jvnLookupObject(jon,this);
			if (obj != null) {
				//Putting the object in the local
				this.hashmapObjects.put(obj.jvnGetObjectId(), obj);
			}
			return obj;
		}catch(Exception e){
			JvnException err = new JvnException(e.getMessage());
			throw(err);
		}
	}	
	
	/**
	* Get a Read lock on a JVN object 
	* @param joi : the JVN object identification
	* @return the current JVN object state
	* @throws  JvnException
	**/
   public Serializable jvnLockRead(int joi)
	 throws JvnException {
		try{
			Serializable res = coordinator.jvnLockRead(joi,this);
			return res;
		}catch(Exception e){
			JvnException err = new JvnException(e.getMessage());
			throw(err);
		}
	}	
	/**
	* Get a Write lock on a JVN object 
	* @param joi : the JVN object identification
	* @return the current JVN object state
	* @throws  JvnException
	**/
   public Serializable jvnLockWrite(int joi)
	 throws JvnException {
		try{
			Serializable res = this.coordinator.jvnLockWrite(joi,this);
			return res;
		}catch(Exception e){
			JvnException err = new JvnException(e.getMessage());
			throw(err);
		}
	}	

	
  /**
	* Invalidate the Read lock of the JVN object identified by id 
	* called by the JvnCoord
	* @param joi : the JVN object id
	* @return void
	* @throws java.rmi.RemoteException,JvnException
	**/
  public void jvnInvalidateReader(int joi)
		  throws java.rmi.RemoteException, jvn.JvnException, InterruptedException {
	  	JvnObject jo = this.hashmapObjects.get(joi);// get jvnobj associated to joi

	  	if ( jo != null)
			jo.jvnInvalidateReader();
  };
	    
	/**
	* Invalidate the Write lock of the JVN object identified by id 
	* @param joi : the JVN object id
	* @return the current JVN object state
	* @throws java.rmi.RemoteException,JvnException
	**/
  public Serializable jvnInvalidateWriter(int joi)
		  throws java.rmi.RemoteException, jvn.JvnException, InterruptedException {
	  	JvnObject jo = this.hashmapObjects.get(joi);// get jvnobj associated to joi
		if ( jo != null)
		  return jo.jvnInvalidateWriter();
		else
		  return null;
  };
	
	/**
	* Reduce the Write lock of the JVN object identified by id 
	* @param joi : the JVN object id
	* @return the current JVN object state
	* @throws java.rmi.RemoteException,JvnException
	**/
   public Serializable jvnInvalidateWriterForReader(int joi)
		   throws java.rmi.RemoteException, jvn.JvnException, InterruptedException {
		JvnObject jo = this.hashmapObjects.get(joi);// get jvnobj associated to joi

	   	if ( jo != null)
		   return jo.jvnInvalidateWriterForReader();
		else
		   return null;
	 };

}

 
