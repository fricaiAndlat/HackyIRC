package de.diavololoop.server.gui;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.function.Consumer;

/**
 * Created by gast1 on 22.05.17.
 */
public class ServerGUI  {

    public final static String LOG_SERVICE = "service";
    public final static String LOG_LISTENER = "listener";
    public final static String LOG_SYSTEM = "system";

    private JFrame frame;

    private HashMap<String, JLog> logs = new HashMap<String, JLog>();
    private HashMap<String, Consumer<String>> listener = new HashMap<String, Consumer<String>>();

    public ServerGUI(){

        addLogToList(LOG_SERVICE);
        addLogToList(LOG_LISTENER);
        addLogToList(LOG_SYSTEM);


        frame = new JFrame("HackyIRCConsole");

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 800);
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);

        frame.setLayout(new GridLayout(1, 0, 20, 20));


        addLogsToFrame();

        frame.setVisible(true);

    }

    private void addLogsToFrame() {

        logs.forEach((key, log) -> frame.add(log));

    }

    private void addLogToList(String name){

        JLog jlog = new JLog(name);
        logs.put(name, jlog);

        listener.put(name, msg -> {});

        jlog.setMessageCallback(this::onMessage);
    }

    private void onMessage(String key, String msg) {
        listener.get(key).accept(msg);
    }

    public void log(String log, String msg){
        JLog l = logs.get(log);

        if(l == null){
            throw new IllegalArgumentException();
        }

        logs.keySet().stream().filter(key -> !key.equals(log)).map(key -> logs.get(key)).forEach(jlog -> jlog.log(""));
        l.log(msg);
    }

    public void setListener(String key, Consumer<String> callback){
        listener.put(key, callback);
    }



}
