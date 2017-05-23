package de.diavololoop.remote;

import java.io.IOException;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

/**
 * Created by Peer on 23.05.2017.
 */
public class Shutdown {

    public static void main(String[] args){

        String portString;

        if(args.length != 1){
            portString = "6667";
        }else {
            portString = args[0];
        }



        try {
            Socket socket = new Socket("localhost", Integer.parseInt(portString));
            socket.getOutputStream().write("SHUT\r\n".getBytes(StandardCharsets.UTF_8));
            socket.getOutputStream().flush();

        } catch (IOException e) {
            e.printStackTrace();
        }


    }

}
