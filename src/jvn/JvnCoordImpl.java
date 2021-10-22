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

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.io.Serializable;
import java.util.List;


public class JvnCoordImpl 	
              extends UnicastRemoteObject 
							implements JvnRemoteCoord{
	

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

    private int compteurID;

    // Map of JVN objects
    private HashMap<Integer, JvnObject> hashmapObjects;
    private HashMap<String, Integer> hashmapName;

    //JVN objects in each remote server
    private HashMap<JvnRemoteServer, ArrayList<JvnObject>> hashmapServerObjects;

    //Gestion des locks (lectures, ecritures)
    //Writer of a JVN object
    private HashMap<Integer, JvnRemoteServer> hashmapLockWrite;
    //Readers of a JVN object
    private HashMap<Integer, ArrayList<JvnRemoteServer>> hashmapLockRead;


    /**
      * Default constructor
      * @throws JvnException
      **/
	private JvnCoordImpl() throws Exception {
        this.compteurID = 0;

        this.hashmapObjects = new HashMap<Integer, JvnObject>();
        this.hashmapName = new HashMap<String, Integer>();
        this.hashmapServerObjects = new HashMap<JvnRemoteServer, ArrayList<JvnObject>>();
        this.hashmapLockWrite = new HashMap<Integer, JvnRemoteServer>();
        this.hashmapLockRead = new HashMap<Integer, ArrayList<JvnRemoteServer>>();

        Registry registry = LocateRegistry.createRegistry(4000);
        registry.bind("coordinator",this);
	}

    /**
    *  Allocate a NEW JVN object id (usually allocated to a
    *  newly created JVN object)
    * @throws java.rmi.RemoteException,JvnException
    **/
    public int jvnGetObjectId()
    throws java.rmi.RemoteException,jvn.JvnException {
        compteurID++;
        return compteurID;
    }
  
  /**
  * Associate a symbolic name with a JVN object
  * @param jon : the JVN object name
  * @param jo  : the JVN object 
  * @param js  : the remote reference of the JVNServer
  * @throws java.rmi.RemoteException,JvnException
  **/
  public void jvnRegisterObject(String jon, JvnObject jo, JvnRemoteServer js)
  throws java.rmi.RemoteException,jvn.JvnException{
    if (this.hashmapObjects.containsKey(jon)) {
        throw new JvnException("[JVN jvnRegisterObject ] Error Object: "+jon+" already registered!");
    }

    hashmapName.put(jon, jo.jvnGetObjectId());
    hashmapObjects.put(jo.jvnGetObjectId(), jo);

    hashmapLockWrite.put(jo.jvnGetObjectId(), js);
    hashmapLockRead.put(jo.jvnGetObjectId(), new ArrayList<JvnRemoteServer>());

    if (!this.hashmapServerObjects.containsKey(js)) {
        hashmapServerObjects.put(js, new ArrayList<JvnObject>());
    }
    hashmapServerObjects.get(js).add(jo);
  }
  
  /**
  * Get the reference of a JVN object managed by a given JVN server 
  * @param jon : the JVN object name
  * @param js : the remote reference of the JVNServer
  * @throws java.rmi.RemoteException,JvnException
  **/
  public JvnObject jvnLookupObject(String jon, JvnRemoteServer js)
  throws java.rmi.RemoteException,jvn.JvnException{
    //Get object
    //System.out.println("coordinator jvnLookupObject");

    Integer id = hashmapName.get(jon);
    JvnObject object = hashmapObjects.get(id);

    if (!this.hashmapServerObjects.containsKey(js)) {
      hashmapServerObjects.put(js, new ArrayList<JvnObject>());
    }
    if(object == null) {
      return null;
    }
    hashmapServerObjects.get(js).add(object);
    object.setLockState(LockState.NL);
    return object;
  }
  
  /**
  * Get a Read lock on a JVN object managed by a given JVN server 
  * @param joi : the JVN object identification
  * @param js  : the remote reference of the server
  * @return the current JVN object state
  * @throws java.rmi.RemoteException, JvnException
  **/
   public Serializable jvnLockRead(int joi, JvnRemoteServer js)
           throws java.rmi.RemoteException, JvnException, InterruptedException {

    if(!hashmapObjects.containsKey(joi) ) {
        throw new JvnException("[JVN jvnLockRead ] Error Object "+joi+" not found!");
    }
    JvnRemoteServer jsWrite = hashmapLockWrite.get(joi);
    Serializable object = null;

    if(jsWrite != null) {
       if(!jsWrite.equals(js)) {
           object = jsWrite.jvnInvalidateWriterForReader(joi);
           hashmapLockWrite.put(joi, null);
           hashmapLockRead.get(joi).add(jsWrite);
       }
        hashmapObjects.get(joi).setObject(object);
    } else {
       object = hashmapObjects.get(joi).jvnGetSharedObject();
    }
    hashmapLockRead.get(joi).add(js);

    return object;

   }

  /**
  * Get a Write lock on a JVN object managed by a given JVN server 
  * @param joi : the JVN object identification
  * @param js  : the remote reference of the server
  * @return the current JVN object state
  * @throws java.rmi.RemoteException, JvnException
  **/
   public Serializable jvnLockWrite(int joi, JvnRemoteServer js)
           throws java.rmi.RemoteException, JvnException, InterruptedException {
        if(!hashmapObjects.containsKey(joi) ) {
            throw new JvnException("[JVN jvnLockRead ] Error Object "+joi+" not found!");
        }

        JvnRemoteServer jsWrite = hashmapLockWrite.get(joi);
        Serializable object = null;

        if(jsWrite != null && !jsWrite.equals(js)) {
            object = jsWrite.jvnInvalidateWriter(joi);
            hashmapLockWrite.put(joi, null);
            hashmapObjects.get(joi).setObject(object);
        }
        else {
           object = hashmapObjects.get(joi).jvnGetSharedObject();
        }

        for(JvnRemoteServer server : hashmapLockRead.get(joi)) {
           if(!server.equals(js)) {
               server.jvnInvalidateReader(joi);
           }
        }
       hashmapLockRead.put(joi, new ArrayList<JvnRemoteServer>());
       hashmapLockWrite.put(joi, js);

        return object;
   }

	/**
	* A JVN server terminates
	* @param js  : the remote reference of the server
	* @throws java.rmi.RemoteException, JvnException
	**/
    public void jvnTerminate(JvnRemoteServer js)
	 throws java.rmi.RemoteException, JvnException {
        ArrayList<JvnObject> listObjects = hashmapServerObjects.get(js);

        for(JvnObject jo : listObjects) {
            List<JvnRemoteServer> jsReaders = hashmapLockRead.get(jo.jvnGetObjectId());
            jsReaders.remove(js);
            JvnRemoteServer jsWriter = hashmapLockWrite.get(jo.jvnGetObjectId());
            if(jsWriter == js) {
                hashmapLockWrite.put(jo.jvnGetObjectId(),null);
            }
        }

        hashmapServerObjects.remove(js);
    }


    public static void main(String[] args) {
        try {
            JvnRemoteCoord jvnCoord = new JvnCoordImpl();
            System.err.println("Coordinator is running ...");

            Thread thread = new Thread(){
                @Override
                public void run() {
                    while(true) {
                        try {
                            if( ! ((JvnCoordImpl) jvnCoord).hashmapObjects.isEmpty()) {
                                for (JvnObject value : ((JvnCoordImpl) jvnCoord).hashmapObjects.values()) {
                                    System.out.println(value);
                                }
                                System.out.println();
                            }else{
                                System.err.println("No object found!");
                            }

                            Thread.sleep(10000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            };
            thread.run();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
