package irc;

import jvn.Annotations;

public interface SentenceProxy {

    @Annotations(type="write")
    public void write(String text);

    @Annotations(type="read")
    public String read();
}