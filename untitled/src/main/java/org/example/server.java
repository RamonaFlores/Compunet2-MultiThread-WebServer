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
        var basePath = new File("").getAbsolutePath();
        var res = new File("untitled/resources/" + resource);
        String contentType = contentType(resource);
        OutputStream out = socket.getOutputStream();
        BufferedOutputStream dataOut = new BufferedOutputStream(out);

        if (res.exists()) {
            System.out.println("[200 OK] Archivo encontrado: " + res.getAbsolutePath());

            long fileLength = res.length();
            String header = "HTTP/1.1 200 OK\r\n" +
                    "Content-Type: " + contentType + "\r\n" +
                    "Content-Length: " + fileLength + "\r\n" +
                    "Connection: close\r\n\r\n";
            dataOut.write(header.getBytes());

            FileInputStream fileIn = new FileInputStream(res);
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = fileIn.read(buffer)) != -1) {
                dataOut.write(buffer, 0, bytesRead);
            }

            fileIn.close();
        } else {
            System.out.println("[ERROR 404] Archivo no encontrado: " + res.getAbsolutePath());
            String errorHtml = "<html><body><h1>404 - Archivo no encontrado</h1></body></html>";
            byte[] errorBytes = errorHtml.getBytes("UTF-8");

            String header = "HTTP/1.1 404 Not Found\r\n" +
                    "Content-Type: text/html\r\n" +
                    "Content-Length: " + errorBytes.length + "\r\n" +
                    "Connection: close\r\n\r\n";
            dataOut.write(header.getBytes());
            dataOut.write(errorBytes);
        }

        dataOut.flush();
        socket.close();
    }
    private static String contentType(String nombreArchivo) {
        String lowerName = nombreArchivo.toLowerCase();

        if (lowerName.endsWith(".html") || lowerName.endsWith(".htm")) return "text/html";
        if (lowerName.endsWith(".css")) return "text/css";
        if (lowerName.endsWith(".js")) return "application/javascript";
        if (lowerName.endsWith(".json")) return "application/json";
        if (lowerName.endsWith(".jpg") || lowerName.endsWith(".jpeg")) return "image/jpeg";
        if (lowerName.endsWith(".png")) return "image/png";
        if (lowerName.endsWith(".gif")) return "image/gif";
        if (lowerName.endsWith(".ico")) return "image/x-icon";
        if (lowerName.endsWith(".svg")) return "image/svg+xml";
        if (lowerName.endsWith(".woff")) return "font/woff";
        if (lowerName.endsWith(".woff2")) return "font/woff2";
        if (lowerName.endsWith(".ttf")) return "font/ttf";
        if (lowerName.endsWith(".pdf")) return "application/pdf";

        // Por defecto, binario genérico
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
