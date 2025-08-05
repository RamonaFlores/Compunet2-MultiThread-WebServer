package org.example;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class server {

    public void init() throws IOException{
        ServerSocket serverSocket = new ServerSocket(8050);
        var isAlive = true;
        while (isAlive) {
            System.out.println("Esperando cliente...");
            var socket = serverSocket.accept();
            System.out.println("¡Cliente conectado!");
            dispatchWorker(socket);

        }
    }
    public void dispatchWorker(Socket socket) throws IOException {
        new Thread(
                ()-> {
                    try {
                        handleRequest(socket);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
        ).start();
    }

    public void handleRequest(Socket socket) throws IOException {
        //Lo que recibimos de chrome
        var reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        //Lo que enviamos a chrome
        var writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        String line;
        while ((line = reader.readLine()) != null && !line.isEmpty()) {
            System.out.println(line);
            if (line.startsWith("GET")){
                //esto se podría utilizar con un string tokenizer
                //el string tokenizer es una lista enlazada con
                //referencias next next next
               var resource =line.split(" ")[1].replace("/", "");
               System.out.println("Client is asking for:"+ resource);

               //Enviar la response
                sendResponse(socket,resource);
            }

        }
    }
    public void sendResponse(Socket socket, String resource) throws IOException {
        var file = new File("");
        System.out.println(file.getAbsolutePath());
        var res = new File("untitled/resources/" + resource);
        var contentType=contentType(resource);
        System.out.println(res.getAbsolutePath());
        var writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

        if (res.exists()) {


            var fis = new FileInputStream(res);
            var br = new BufferedReader(new InputStreamReader(fis));
            String line;
            StringBuilder response = new StringBuilder();
            while ((line = br.readLine()) != null) {
                response.append(line);
            }

            //Response

            writer.write("HTTP/1.1 200 OK\r\n");
            writer.write("Content-Type:"+contentType+"\r\n");
            writer.write("Content-Length: " + response.length() + "\r\n");
            writer.write("Connection: close\r\n");
            writer.write("\r\n");
            writer.write(response.toString());

            writer.close();
            socket.close();

        } else {
            String response = "";

            //Manejar error
            System.out.println("no se encuentra ningún archivo");
            writer.write("HTTP/1.1 404 Not Found\r\n");
            writer.write("Content-Type:"+contentType+"\r\n");
            writer.write("Content-Length: 0\r\n");
            writer.write("Connection: close\r\n");
            writer.write("\r\n");
            writer.write(response);
            writer.close();
            socket.close();
        }
    }
    private static String contentType(String nombreArchivo) {
        if(nombreArchivo.endsWith(".htm") || nombreArchivo.endsWith(".html")) {
            return "text/html";
        }
        if(nombreArchivo.endsWith(".jpg")) {
            return "image/jpeg";
        }
        if(nombreArchivo.endsWith(".gif")) {
            return "image/gif";
        }
        return "application/octet-stream";
    }
    public static void main(String[] args) throws IOException {
        server s = new server();
        s.init();

            /*
            //Response
            var response = "<html><body><h1>Hola a todos</h1></body></html>";
            writer.write("HTTP/1.1 200 OK\r\n");
            writer.write("Content-Type: text/html\r\n");
            writer.write("Content-Length: " + response.length()+"\r\n");
            writer.write("Connection: close\r\n");
            writer.write("\r\n");
            writer.write(response);

            writer.close();
            socket.close();

             */

    }
}
