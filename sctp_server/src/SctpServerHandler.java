import dao.GameDAO;
import org.json.simple.JSONObject;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

public class SctpServerHandler extends Thread{

    private static final Map<String, String> CONTENT_TYPES = new HashMap<>() {{
        put("jpg", "image/jpeg");
        put("html", "text/html");
        put("json", "application/json");
        put("txt", "text/plain");
        put("", "text/plain");
    }};

    private Socket socket;
    private String directory;
    private String method;
    private String requestURL;
    private String Host;
    private String UserAgent;
    private String requestPayload;

    private DateTimeFormatter DTF = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");

    private GameDAO gameDAO;

    public SctpServerHandler(Socket socket, String directory) {
        this.socket = socket;
        this.directory = directory;
        this.gameDAO = new GameDAO();
        this.gameDAO.connect();
    }

    @Override
    public void run() {
        try (var input = this.socket.getInputStream(); var output = this.socket.getOutputStream()) {
            parseRequest(input);
            System.out.println("NOW we have method -  " + method);
            System.out.println("NOW we have url -  " + requestURL);
            // first here
            switch (method){
                case "GET": {
                    if (isRequestInDatabase(requestURL)) {
                        String request = requestURL.split("\\?")[0];


                        switch (request){
                            case "/game_id":{
                                String name = requestURL.split("\\?")[1].split("=")[1].replace("+", " ");
                                this.view_game_id(output, name);
                                break;
                            }
                            case "/game": {  // view_game
                                System.out.println("find it");
                                // this.gameDAO.getFullInfoAboutGame(name);
                                String name = requestURL.split("\\?")[1].split("=")[1].replace("+", " ");
                                this.gameDAO.getFullInfoAboutGame(name);
                                sendHttpMessage(output, HTTP_MESSAGE.FORBIDDEN_403, 403);
                                break;
                            }
                            default: {
                                break;
                            }
                        }
                    } else {
                        var filePath = Path.of(this.directory, requestURL);
                        if (Files.exists(filePath) && !Files.isDirectory(filePath)) {
                            var extension = this.getFileExtension(filePath);
                            var type = CONTENT_TYPES.get(extension);
                            var fileBytes = Files.readAllBytes(filePath);
                            this.sendHeader(output, 200, "OK", type, fileBytes.length);

                            LogSystem.acces_log(Host, DTF.format(LocalDateTime.now()).toString(),method + " " +
                                    requestURL + "HTTP/1.1", 200,fileBytes.length,requestURL,UserAgent);

                            output.write(fileBytes);
                            output.flush();
                        } else {
                            sendHttpMessage(output, HTTP_MESSAGE.NOT_FOUND_404, 404);
                        }
                    }
                    break;
                }
                case "POST":{
                    switch (requestURL){
                        default: {
                            sendHttpMessage(output, HTTP_MESSAGE.FORBIDDEN_403, 403);
                            break;
                        }
                    }
                    break;
                }
                default: {
                    sendHttpMessage(output, HTTP_MESSAGE.FORBIDDEN_403, 403);
                    break;
                }
            }

        } catch (IOException e){
            e.printStackTrace();
        }
    }

    private void view_game_id(OutputStream output, String name) throws IOException {
        System.out.println(name);
        entities.Game game = gameDAO.getGame(name);

        if(game == null){
            sendHttpMessage(output, HTTP_MESSAGE.NOT_FOUND_404, 404);
            //this.redirect("view_game", output);пуеПф
            return;
        }
        String type = CONTENT_TYPES.get("application/json");

        JSONObject jsonObject = game.getGameAsJSONObject();
        String jsonString = jsonObject.toString();
        System.out.println("json string = " + jsonString);

        this.sendHeader(output, 200, HTTP_MESSAGE.OK_200, type, jsonString.length());
        sendContent(output, jsonString, "windows-1251");
        output.flush();
    }

    private void redirect(String url, OutputStream output){
        try {
            output.write("HTTP/1.1 301 Moved Permanently\r\n".getBytes());
            output.write(("Location: " + url + "\r\n\n").getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendHttpMessage(OutputStream output, String message, int statusCode) {
        try {
            String type = CONTENT_TYPES.get("text");
            this.sendHeader(output, statusCode, message, type, message.length());

            LogSystem.acces_log(Host, DTF.format(LocalDateTime.now()).toString(), method + " " +
                            requestURL + "HTTP/1.1", statusCode, message.length(),
                    requestURL, UserAgent);

            System.out.println(message);
            sendContent(output, message, "windows-1251");
        } catch (IOException e){
            e.printStackTrace();
        }

    }

    private String getFileExtension(Path path){
        String name = path.getFileName().toString();
        int extensionStart = name.lastIndexOf(".");
        return  extensionStart == -1 ? "" : name.substring(extensionStart + 1);
    }

    private void sendHeader(OutputStream output, int statusCode, String statusText, String type, long lenght) throws IOException {
        var ps = new PrintStream(output);
        ps.printf("HTTP/1.1 %s %s%n", statusCode, statusText);
        ps.printf("Date: %s%n", DTF.format(LocalDateTime.now())); ////////////////
        ps.printf("Content-Type: %s%n", type);
        ps.printf("Content-Length: %s%n%n", lenght);
    }

    private void sendContent(OutputStream output, String content, String charsetName) {
        try {
            PrintWriter pw = new PrintWriter(new OutputStreamWriter(output, charsetName), true);
            pw.println(content);
        } catch(IOException e) {
            System.err.println("Err in sendContent function.(SCTP server handler)");
            e.printStackTrace();
        }
    }

    private Boolean isRequestInDatabase(String str){
        return str.contains("?");
    }

    private void parseRequest(InputStream input) throws IOException {
        InputStreamReader isReader = new InputStreamReader(input,"UTF-8");
        BufferedReader br = new BufferedReader(isReader);
        //code to read and print headers
        String firstLine = br.readLine();
        method = firstLine.split(" ")[0];
        requestURL = firstLine.split(" ")[1];
        Host = br.readLine().split(" ")[1];;
        System.out.println("Host = " + Host);
        System.out.println("firstLine = " + firstLine);
        String headerLine = null;
        while((headerLine = br.readLine()).length() != 0){
            // System.out.println(headerLine);
            if(headerLine.contains("User-Agent")){
                UserAgent = headerLine.split(" ",2)[1];
            }
        }

        StringBuilder payload = new StringBuilder();
        while(br.ready()){
            payload.append((char) br.read());
        }

        requestPayload = payload.toString();
        System.out.println("method = " + method);
        System.out.println("Request payload is: " + requestPayload);
    }
}