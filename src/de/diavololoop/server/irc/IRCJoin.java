package de.diavololoop.server.irc;

/**
 * Created by Peer on 23.05.2017.
 */
public class IRCJoin {

    private String channel;
    private String username;

    public IRCJoin(String username, String channel) {
        this.channel = channel;
        this.username = username;
    }

    @Override
    public String toString() {
        return IRCJoin.toString(username, channel);
    }

    public static String toString(String username, String channel){
        return ":" + username + "!~" + username +"@localhost JOIN "+channel;
    }

}
