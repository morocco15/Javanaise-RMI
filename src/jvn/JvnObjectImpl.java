package jvn;

import java.io.Serializable;

public class JvnObjectImpl implements JvnObject {
    //TODO: Wait et Notify
    private int id;
    private LockState lockState;
    private Serializable objetpartage;


    public JvnObjectImpl(int id, Serializable o){
        this.lockState = LockState.WLT;
        this.id = id;
        this.objetpartage = o;
    }

    public void setLockState(LockState lockState) {
        this.lockState = lockState;
    }

    public void setObjetpartage(Serializable objetpartage) {
        this.objetpartage = objetpartage;
    }

    @Override
    public void jvnLockRead() throws JvnException {
        switch(this.lockState){
            case NL :
                JvnServerImpl.jvnGetServer().jvnLockRead(this.jvnGetObjectId());
                this.setLockState(LockState.RLT);
                break;
            case RLC :
                this.setLockState(LockState.RLT);
                break;
            case WLC :
                this.setLockState(LockState.RLT_WLC);
                break;
            //case RLT :
            //case WLT :
            //case RLT_WLC :*/
            default :
                break;
        }
        
    }


    @Override
    public void jvnLockWrite() throws JvnException, InterruptedException {
        switch(this.lockState){
            case NL :
            case RLC :
                JvnServerImpl.jvnGetServer().jvnLockWrite(this.jvnGetObjectId());
                this.setLockState(LockState.WLT);
                break;
            case WLC :
                this.setLockState(LockState.WLT);
                break;

            default :
                break;
        }
        
    }


    @Override
    public void jvnUnLock() throws JvnException, InterruptedException {
        switch(this.lockState){
            case RLC :
            case RLT :
                this.jvnInvalidateReader();
                break;
            case WLC :
            case WLT :
                this.jvnInvalidateWriter();
               // notify();
                break;
            case RLT_WLC :
                this.jvnInvalidateReader();
                this.jvnInvalidateWriter();
                //
                break;
            default :
                break;
        }
        notify();
    }

    public int jvnGetObjectId() throws JvnException {
        return this.id;
    }

    public Serializable jvnGetSharedObject() throws JvnException {
        return this.objetpartage;
    }

    public void jvnInvalidateReader() throws JvnException, InterruptedException {
        switch(this.lockState){
            case RLC :
                this.setLockState(LockState.NL);
                break;
            case RLT :
            case RLT_WLC :
                wait();
                break;
            default :
                break;
        }
    }

    public Serializable jvnInvalidateWriter() throws JvnException, InterruptedException {
        switch(this.lockState){
            case WLC : this.setLockState(LockState.NL);
            case WLT : wait();
            case RLT_WLC : this.setLockState(LockState.RLT);
            default :
                break;
        }

        return this.objetpartage;
    }

    public Serializable jvnInvalidateWriterForReader() throws JvnException, InterruptedException {
        //Not
        switch(this.lockState){
            case WLC :
                this.setLockState(LockState.RLT);
            case WLT :
                wait();
                this.setLockState(LockState.RLT);
            case RLT_WLC : this.setLockState(LockState.RLT);
            default :
                break;
        }

        return this.objetpartage;
    }

    @Override
    public String toString() {
        return  "ObjectID = " + this.id +"  LockState = "+ this.lockState.toString() + "  Value = " + this.objetpartage;
    }
}
