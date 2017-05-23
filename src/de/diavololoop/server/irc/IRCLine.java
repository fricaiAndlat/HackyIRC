package de.diavololoop.server.irc;

/**
 * Created by Peer on 21.05.2017.
 */
public abstract class IRCLine {

    public final long timestamp;
    protected String channel;

    protected IRCLine(String channel){
        this.timestamp = System.currentTimeMillis();
        this.channel = channel;
    }

    protected IRCLine(){
        this("undefined");
    }

    public static IRCLine read(String s){

        if(s.toLowerCase().matches(".* privmsg .*:.*")){
            return new IRCMessage(s);
        }

        return null;
    }

    public String getChannel(){
        return channel;
    }

}
