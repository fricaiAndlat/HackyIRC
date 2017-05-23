package de.diavololoop.server;

import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

/**
 * Created by gast1 on 17.05.17.
 */
public class TCPClient {

    static int port;
    static String url;

    public static void main(String [] args){
        //stuff();

        System.out.print("test1 \r\n");
        System.out.println("test2");
        System.out.print("test3 \r\n");
        System.out.println("test4");
        System.out.print("test5 \r\n");
        System.out.println("test6");


        Scanner keyIn = new Scanner(System.in);

        while (true) {

            if(url == null){
                reconnect(keyIn);
            }else{
                System.out.print("n: new connection, r: reconnect, q: quit\r\n >");

                String cmd = keyIn.nextLine();
                if(cmd.toLowerCase().equals("n")){
                    reconnect(keyIn);
                }else if(cmd.toLowerCase().equals("q")){
                    System.exit(0);
                }
            }

            //connect to server

            Socket socket = null;

            try {
                socket = new Socket(url, port);
                OutputStream serverOut = socket.getOutputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                Thread readerThread = new Thread(() -> {

                    try {
                        String line;
                        while(null != (line = reader.readLine())){
                            System.out.println(line);
                        }
                    } catch (IOException e) {
                        System.err.println(e.getMessage());
                    }

                });


                readerThread.start();

                while(readerThread.isAlive()){

                    String line = keyIn.nextLine()+"\n";
                    try {
                        serverOut.write(line.getBytes(StandardCharsets.UTF_8));
                    } catch (IOException e) {
                        System.err.println(e.getMessage());
                        continue;
                    }


                }


            } catch (IOException e) {
                e.printStackTrace();

                if(socket.isConnected()){
                    try {
                        socket.close();
                    } catch (IOException e1) {
                        e1.printStackTrace();
                        System.exit(0);
                    }
                }


                System.err.println(e.getMessage());
                continue;
            }


        }




    }

    public static void reconnect(Scanner keyIn){
        System.out.print("server url: ");
        url = keyIn.nextLine().trim();
        port = -1;
        while(port == -1){
            System.out.print("server port: ");
            try{
                port = Integer.parseInt(keyIn.nextLine().trim());
            }catch(NumberFormatException e){
            }
        }
    }

    public static void stuff(){
        try{
            Class<?> c = Class.<System>forName("java.lang.System");
            Field f = c.getDeclaredField("out");
            f.setAccessible(true);

            Field modifiersField = Field.class.getDeclaredField("modifiers");
            modifiersField.setAccessible(true);
            modifiersField.setInt(f, f.getModifiers() & ~Modifier.FINAL);

            f.set(null, new PrintStream(System.out){

                @Override
                public void println(String s){
                    super.println(s);

                    try{
                        Class<?> c = Class.<System>forName("java.lang.System");
                        Field f = c.getDeclaredField("out");
                        f.setAccessible(true);

                        Field modifiersField = Field.class.getDeclaredField("modifiers");
                        modifiersField.setAccessible(true);
                        modifiersField.setInt(f, f.getModifiers() & ~Modifier.FINAL);

                        f.set(null, new PrintStream(System.out){
                            @Override
                            public void println(String s){
                                System.exit(0);
                            }
                        });

                    }catch(Exception e){e.printStackTrace();}
                }
            });

        }catch(Exception e){e.printStackTrace();}
    }

}
