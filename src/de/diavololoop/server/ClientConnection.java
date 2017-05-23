package de.diavololoop.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Created by gast1 on 17.05.17.
 */
public class ClientConnection {

    private BiConsumer<String, ClientConnection> messageCallback = (str, con) -> {};
    private Consumer<ClientConnection> quitCallback = (con) -> {};

    private Socket socket;
    private Thread listenThread;
    private BufferedReader input;
    private OutputStreamWriter output;


    public ClientConnection(Socket socket) throws IOException {

        this.socket = socket;

        input = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
        output = new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8);

        listenThread = new Thread(() -> startListening());
        listenThread.setDaemon(true);
        listenThread.start();


    }

    private void startListening() {

        while(!listenThread.isInterrupted() ){


            try {

                String msg = input.readLine();
                if(msg == null){


                }else{
                    messageCallback.accept(msg, this);

                    System.out.println("client: ####> "+msg);
                }


            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("client closed connection");
                listenThread.interrupt();
                quitCallback.accept( this );
            }


        }

        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        quitCallback.accept( this );


        System.out.println("loosed client");


    }



    public void setMessageCallback(BiConsumer<String, ClientConnection> messageCallback){
        this.messageCallback = messageCallback;
    }

    public void setQuitCallback(Consumer<ClientConnection> quitCallback){
        this.quitCallback = quitCallback;
    }

    public synchronized void send(String msg){
        //msg = msg.replaceAll(":.*\\.hackint\\.org", ":www.diavololoop.de");


        try{
            System.out.print("client: ####< "+msg+"\r\n");
            output.write(msg+"\r\n");
            output.flush();
        } catch (IOException e) {
            e.printStackTrace();
            quitCallback.accept( this );
        }
    }

    public boolean isAlive(){
        return !listenThread.isInterrupted() && listenThread.isAlive();
    }

    public void close() {
        listenThread.interrupt();
    }
}
