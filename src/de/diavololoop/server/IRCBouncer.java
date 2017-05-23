package de.diavololoop.server;

import de.diavololoop.server.gui.ServerGUI;
import de.diavololoop.server.irc.IRCHackyIntern;
import de.diavololoop.server.irc.IRCJoin;
import de.diavololoop.server.irc.IRCLine;
import de.diavololoop.server.irc.IRCMessage;

import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedList;

/**
 * Created by gast1 on 17.05.17.
 */
public class IRCBouncer {

    private static String   IRC_SERVER_URL = "irc.hackint.org";
    private static int      IRC_SERVER_PORT = 6667;
    private static boolean  USE_GUI = true;

    public static void main(String[] args) throws IOException {

        int port = -1;
        String username = null;
        String serverURL = null;


        for(int i = 0; i < args.length; ++i){

            if(args[i].equalsIgnoreCase("-h")){

                printHelp();
                return;

            }else if(args[i].equalsIgnoreCase("-p")){

                if(i + 1 == args.length){
                    printHelp();
                    return;
                }
                if(!args[i + 1].matches("\\d{1,5}")){
                    System.out.println("port must be numeric and less than 100000");
                    return;
                }
                port = Integer.parseInt(args[i + 1]);
                ++i;

            }else if(args[i].equalsIgnoreCase("-u")){

                if(i + 1 == args.length){
                    printHelp();
                    return;
                }
                if(!args[i + 1].matches("[a-zA-Z0-9_-]{1,20}")){
                    System.out.println("user must be in a-zA-Z0-9_-");
                    return;
                }
                username = args[i + 1];
                ++i;

            }else if(args[i].equalsIgnoreCase("-s")){

                if(i + 1 == args.length){
                    printHelp();
                    return;
                }
                serverURL = args[i + 1];
                ++i;

            }else if(args[i].equalsIgnoreCase("-nogui")){

                USE_GUI = false;

            }

        }

        if(username == null || port == -1 || serverURL == null){
            printHelp();
            return;
        }

        IRC_SERVER_PORT = port;
        IRC_SERVER_URL = serverURL;

        IRCBouncer bouncer = new IRCBouncer();
        bouncer.startServer(username);


    }

    public static void printHelp(){
        System.out.println("\t-h\t\t\tprints help");
        System.out.println("\t-u\t[user]\tuser used to connect to the IRC");
        System.out.println("\t-p\t[port]\tport from IRC and for the bouncer service");
        System.out.println("\t-s\t[url]\turl from IRC");
        System.out.println("\t-nogui\t\t\tstart without gui");
    }


    private ServerGUI gui;

    private Listener listener;
    private IRCService service;

    private LinkedList<IRCLine> history = new LinkedList<IRCLine>();
    private HashSet<String> channels = new HashSet<String>();

    private String username;

    public void  startServer(String username) throws IOException {

        this.username = username;

        listener = new Listener(IRC_SERVER_URL, IRC_SERVER_PORT, username, this::onIRCMessage);
        service = new IRCService(IRC_SERVER_PORT);

        if(USE_GUI){
            gui = new ServerGUI();
            gui.setListener(ServerGUI.LOG_LISTENER, listener::send);
            gui.setListener(ServerGUI.LOG_SERVICE, service::send);
        }




        service.setMessageCallback(this::onClientMessage);
        service.setNewClientCallback(this::onClientJoin);
    }

    private void onClientJoin(ClientConnection clientConnection) {

        service.send("PING :WELCOME");

        service.send(  IRCJoin.toString(username, IRCHackyIntern.ERROR_CHANNEL)  );

        channels.forEach(channel -> {

            service.send(IRCJoin.toString(username, channel));

            listener.send("NAMES "+channel);

        });

        history.stream().map(ircLine -> ircLine.toString()).forEach(service::send);




    }

    private void onClientMessage(String msg, ClientConnection con){
        if(USE_GUI){
            gui.log(ServerGUI.LOG_SERVICE, "> "+msg);
        }

        if(!filterMessage(msg)){
            return;
        }


        if(msg.toLowerCase().matches("join .*")){
            String channelname = msg.substring(msg.indexOf(" "));

            if(channels.contains(channelname)){



                history .stream()
                        .filter( message -> message.getChannel().equalsIgnoreCase(channelname) )
                        .map( message -> message.toString() )
                        .forEach(listener::send);

                msg = "NAMES "+channelname;

            }else{

                channels.add(channelname);

            }

        }else if(msg.toLowerCase().matches("privmsg .*:.*")){
            IRCMessage ircMSG = new IRCMessage(username+"!~"+username+"@system" + " " + msg);

            synchronized (history) {
                history.addLast(ircMSG);
            }

        }else if(msg.toLowerCase().matches("hist.*")){
            history.stream().map(ircLine -> ircLine.toString()).forEach(con::send);
            return;
        }else if(msg.toLowerCase().matches("shut")){

            service.quitConnection();
            listener.close();
            System.exit(0);
            return;

        }else if(msg.toLowerCase().matches("stat")){

            con.send(new IRCHackyIntern("INFO:").toString());
            con.send(new IRCHackyIntern("connections: " + service.getActiveConnections()).toString());

            return;

        }


        listener.send(msg);

    }

    private void onIRCMessage(String msg){
        if(USE_GUI){
            gui.log(ServerGUI.LOG_LISTENER, "> "+msg);
        }

        if(msg.toLowerCase().startsWith("ping :")){
            listener.send("PONG "+msg.split(":")[1]);
            return;
        }

        IRCLine ircLine = IRCLine.read(msg);

        if(ircLine != null){

            synchronized (history) {
                history.addLast(ircLine);
            }
        }

        service.send(msg);
    }

    private boolean filterMessage(String msg){

        if(msg.toLowerCase().matches("nick .*")){
            return false;
        }

        if(msg.toLowerCase().matches("user .*")){
            return false;
        }

        if(msg.toLowerCase().matches("quit.*")){
            service.quitConnection();
            return false;
        }

        if(msg.toLowerCase().matches("pong .*")){
            return false;
        }

        return true;

    }


}
