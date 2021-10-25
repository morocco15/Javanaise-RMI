package jvn;

import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

public class JvnProxy implements InvocationHandler {

    JvnObject object;

    public JvnProxy(JvnObject object) {
        this.object = object;
    }

    public static Object newInstance(Object obj, String name) {
        JvnServerImpl js = JvnServerImpl.jvnGetServer();

        JvnObject jvnObject = null;
        try {
            jvnObject =  js.jvnLookupObject(name);

            if (jvnObject == null) {
                jvnObject = js.jvnCreateObject((Serializable) obj);
                jvnObject.jvnUnLock();
                js.jvnRegisterObject(name, jvnObject);
            }
        } catch (JvnException | InterruptedException e) {
            System.out.println("[JVN Proxy] newInstance : " + e.getMessage());
        }

        return java.lang.reflect.Proxy.newProxyInstance(
                obj.getClass().getClassLoader(),
                obj.getClass().getInterfaces(),
                new JvnProxy( jvnObject));
    }

    @Override
    public Object invoke(Object proxyObj, Method method, Object[] args) throws Throwable {
        Object result;
        try {
            if(method.isAnnotationPresent(Annotations.class)) {
                if(method.getAnnotation(Annotations.class).type().equals("read")) {
                    this.object.jvnLockRead();
                }else if(method.getAnnotation(Annotations.class).type().equals("write")) {
                    this.object.jvnLockWrite();
                } else {
                    throw new JvnException("[JVN invoke Type Error]");
                }
            }

            result = method.invoke(this.object.jvnGetSharedObject(), args);
            this.object.jvnUnLock();
        } catch (Exception e) {
            throw new JvnException("[JVN invoke Exception]: "+ e.getMessage());
        }
        return result;
    }
}
