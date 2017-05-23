package de.diavololoop.server.irc;

/**
 * Created by Peer on 21.05.2017.
 */
public class IRCMessage extends IRCLine {

    private String nameFirst;
    private String nameSecond;
    private String content;

    protected IRCMessage(String nameFirst, String nameSecond, String channel, String content){

        super(channel);
        this.nameFirst  = nameFirst;
        this.nameSecond = nameSecond;

        this.content = content;
    }

    public IRCMessage(String msg){
        if(!msg.toLowerCase().contains("privmsg")){
            throw new IllegalArgumentException();
        }

        if(msg.startsWith(":")){
            msg = msg.substring(1);
        }

        String name = msg.split(" PRIVMSG ")[0].trim();
        nameFirst  = name.substring(0, name.indexOf("!"));
        nameSecond = name.substring(name.indexOf("!"));

        String channelWithText = msg.split(" PRIVMSG ")[1].trim();

        if(!channelWithText.toLowerCase().contains(":")){
            throw new IllegalArgumentException();
        }

        super.channel = channelWithText.split(":")[0].trim();

        content = channelWithText.substring(1 + channelWithText.indexOf(":"));


    }

    @Override
    public String toString(){

        String filled = "";

        for(int i = 0; i < 15-nameFirst.length(); ++i){
            filled = filled + " ";
        }

        return String.format(":%s%s PRIVMSG %s :%s%tR %s",
                nameFirst, nameSecond, channel, filled, super.timestamp, content);
    }

}
