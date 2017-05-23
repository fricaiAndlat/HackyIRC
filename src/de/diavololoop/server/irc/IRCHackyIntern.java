package de.diavololoop.server.irc;

/**
 * Created by Peer on 21.05.2017.
 */
public class IRCHackyIntern extends IRCMessage {

    public final static String ERROR_CHANNEL = "#HackyIRC";

    public IRCHackyIntern(String s) {

        super("HackyIRC", "!~hackyirc@system", ERROR_CHANNEL, s);

    }
}
