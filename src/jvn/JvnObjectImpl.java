/***
 * JAVANAISE Implementation
 * JvnServerImpl class
 * Implementation of a Jvn server
 * Contact:
 *
 * Authors:
 * 		Walid BOUKRIS
 * 	    Labib
 */

package jvn;

import java.io.Serializable;

public class JvnObjectImpl implements JvnObject {

    //Attributes
    private int id;
    private LockState lockState;
    private Serializable object;

    //Constructor
    public JvnObjectImpl(int id, Serializable o){
        this.lockState = LockState.WLT;
        this.id = id;
        this.object = o;
    }

    public void setLockState(LockState lockState) {
        this.lockState = lockState;
    }

    public void setObject(Serializable objetpartage) {
        this.object = objetpartage;
    }

    public int jvnGetObjectId() {
        return this.id;
    }

    public Serializable jvnGetSharedObject() {
        return this.object;
    }

    @Override
    public String toString() {
        return  "ObjectID = " + this.id +"  LockState = "+ this.lockState.toString() + "  Value = " + this.object;
    }

    public void jvnLockRead() throws JvnException {
        switch(this.lockState){
            case NL :
            case RLT:/*  Anyone can READ !*/
                this.object = JvnServerImpl.jvnGetServer().jvnLockRead(this.jvnGetObjectId());
                this.setLockState(LockState.RLT);
                break;
            case RLC :
                this.setLockState(LockState.RLT);
                break;
            case WLC :
                this.setLockState(LockState.RLT_WLC);
                break;
            default :// RLT, WLT, RLT_WLC
                throw new JvnException("[JVN Lock Read] Error state: "+this.lockState);
        }
        
    }

    public void jvnLockWrite() throws JvnException, InterruptedException {
        switch(this.lockState){
            case NL :
                JvnServerImpl.jvnGetServer().jvnLockWrite(this.jvnGetObjectId());
                this.setLockState(LockState.WLT);
            case RLC :
            case RLT :
                JvnServerImpl.jvnGetServer().jvnLockWrite(this.jvnGetObjectId());
                this.setLockState(LockState.WLT);
                break;
            case WLC :
                this.setLockState(LockState.WLT);
                break;
            default :// RLT, WLT, RLT_WLC
                throw new JvnException("[JVN Lock Write] Error state: "+this.lockState);
        }
    }

    public synchronized void jvnUnLock() throws JvnException {
        switch(this.lockState){
            case RLT :
                this.lockState = LockState.RLC;
                break;
            case WLT :
            case RLT_WLC :
                this.lockState = LockState.WLC;
                break;
            default :// NL, WLC, RLC
                throw new JvnException("[JVN UnLock] Error state: "+this.lockState);
        }
        try{
            this.notify();
        }catch (Exception e){
            throw new JvnException("[JVN UnLock] notify:"+e.getMessage());
        }

    }

    public synchronized void jvnInvalidateReader() throws JvnException, InterruptedException {
        //System.out.println("ICI La merde ce passe hahaha"+this.lockState);
        switch(this.lockState){
            case RLC :
            case RLT :
            case RLT_WLC :
                /*try {
                    this.wait();
                } catch (InterruptedException e) {
                    throw new JvnException("[JVN jvnInvalidateReader (InterruptedException) ] Error : "+e.getMessage());
                }*/
                this.setLockState(LockState.NL);
                break;
            case NL:
                break;
            default :// NL, WLC, WLT
                throw new JvnException("[JVN jvnInvalidateReader ] Error state: "+this.lockState);
        }
    }

    public synchronized Serializable jvnInvalidateWriter() throws JvnException, InterruptedException {
        switch(this.lockState){
            case WLC :
                this.setLockState(LockState.NL);
                break;
            case WLT :
                this.wait();
                break;
            case RLT_WLC :
                this.setLockState(LockState.RLT);
                break;
            default :// NL, RLC, RLT
                throw new JvnException("[JVN jvnInvalidateWriter ] Error state: "+this.lockState);
        }

        return this.object;
    }

    public synchronized Serializable jvnInvalidateWriterForReader() throws JvnException, InterruptedException {
        switch(this.lockState){
            case WLC :
            case RLT_WLC :
                this.setLockState(LockState.RLT);
                break;
            case WLT :
                try {
                    this.wait();
                    this.setLockState(LockState.RLT);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                break;
            default :// NL, RLC, RLT
                throw new JvnException("[JVN InvalidateWriterForReader ] Error state: "+this.lockState);
        }
        return this.object;
    }
}
/*
        switch (this.lockState) {
            case RLT_WLC:
            case WLT:
                    this.wait();
                    this.setLockState(LockState.RLT);
            case WLC:
                    this.etat = Etat.RLC;
*/


