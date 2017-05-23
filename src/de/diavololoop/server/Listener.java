package de.diavololoop.server;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;

/**
 * Created by gast1 on 17.05.17.
 */
public class Listener {

    private Socket socket;

    private String url;
    private int port;

    private String username;

    private BufferedReader reader;
    private OutputStreamWriter writer;


    private Consumer<String> messageCallback = null;
    private Consumer<String> errorCallback = (err) -> {};

    private Thread listenerThread;

    private String fatalError = null;



    public Listener(String url, int port, String username, Consumer<String> messageCallback){
        this.url = url;
        this.port = port;
        this.username = username;

        this.messageCallback = messageCallback;

        try {

            connect();

        } catch (IOException e) {
            fatalError = e.getMessage();
        }


        init();




    }

    public void close(){
        if(fatalError != null){
            throw new IllegalStateException("previous exception: "+fatalError);
        }

        send("QUIT");
        listenerThread.interrupt();
    }

    private void startListen(){
        //sleep for letting the references in callbacks built up
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            listenerThread.interrupt();
        }

        while(!listenerThread.isInterrupted()){

            listen();

        }

    }


    public void setMessageCallback(Consumer<String> callback){

        messageCallback = callback;

    }

    private void connect() throws IOException {

        socket = new Socket(url, port);

        reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
        writer = new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8){
            @Override
            public void write(String s) throws IOException {
                super.write(s);
                System.out.print("irc: ###< "+s.replaceAll("\n", "\r\n"));
            }
        };

        listenerThread = new Thread(() -> startListen());
        listenerThread.start();

    }
    private void init(){

        try { Thread.sleep(200); } catch (InterruptedException e) { listenerThread.interrupt(); }

        send("USER "+username+" 0 this "+username+"\r\n");

        try { Thread.sleep(200); } catch (InterruptedException e) { listenerThread.interrupt(); }

        send("NICK "+username+"\r\n");

        try { Thread.sleep(200); } catch (InterruptedException e) { listenerThread.interrupt(); }

        //send("JOIN "+channel+"\r\n");

    }



    public synchronized void send(String msg) {



        try{

            writer.write(msg + "\r\n");
            writer.flush();

        } catch (IOException e){

            onError(e.getMessage());

        }

    }

    private void listen() {

        try {

            String line = reader.readLine();

            if(line == null){
                listenerThread.interrupt();

                connect();

                return;
            }

            if(messageCallback != null){
                messageCallback.accept( line );
            }

            System.out.println("irc: ###> "+line);


        } catch (IOException e){
            onError(e.getMessage());
        }

    }

    private void onError(String desc){

        errorCallback.accept(desc);

        if(!socket.isClosed()){
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try {
            connect();
            init();
        } catch (IOException e) {
            fatalError = e.getMessage();
        }


    }

    public String getFatalError() {
        return fatalError;
    }

}
