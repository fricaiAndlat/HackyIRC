package de.diavololoop.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Created by gast1 on 17.05.17.
 */
public class IRCService {

    private ServerSocket serverSocket;
    private Thread acceptThread;

    private LinkedList<ClientConnection> clients = new LinkedList<ClientConnection>();

    private BiConsumer<String, ClientConnection> messageCallback = (msg, con) -> {};
    private Consumer<ClientConnection> newClientCallback = (client) -> {};


    public IRCService( int port ) throws IOException {

        serverSocket = new ServerSocket(port);

        acceptThread = new Thread(() -> startListening());
        acceptThread.setDaemon(true);
        acceptThread.setName("IRCService accept Thread");
        acceptThread.start();

    }

    public int getActiveConnections() {
        return clients.size();
    }

    private void startListening() {

        while(!acceptThread.isInterrupted()){

            try {
                Socket socket = serverSocket.accept();
                ClientConnection con = new ClientConnection(socket);

                System.out.println("--new client--");

                con.setQuitCallback(this::onQuit);
                con.setMessageCallback(messageCallback);

                newClientCallback.accept(con);

                clients.add(con);

            } catch (IOException e) {
                acceptThread.interrupt();
            }

        }
    }


    private void onQuit(ClientConnection con) {
        clients.remove(con);
    }

    public void setMessageCallback(BiConsumer<String, ClientConnection> messageCallback){

        this.messageCallback = messageCallback;
        clients.forEach(  con -> con.setMessageCallback(messageCallback)  );

    }

    public void setNewClientCallback(Consumer<ClientConnection> callback){
        newClientCallback = callback;
    }

    public void send(String msg) {
        clients.forEach(  con -> con.send(msg)  );
    }

    public void quitConnection() {

        clients.forEach(  con -> con.close()  );

    }
}
