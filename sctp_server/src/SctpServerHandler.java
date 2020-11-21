import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
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

    public SctpServerHandler(Socket socket, String directory) {
        this.socket = socket;
        this.directory = directory;
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
                        var type = CONTENT_TYPES.get("text");
                        String message = HTTP_MESSAGE.NOT_FOUND_404;
                        this.sendHeader(output, 404, message, type, message.length());

                        LogSystem.acces_log(Host, DTF.format(LocalDateTime.now()).toString(), method + " " +
                                        requestURL + "HTTP/1.1", 404, message.length(),
                                requestURL, UserAgent);
                        
                        System.out.println(message);
                        sendContent(output, message, "windows-1251");
                    }
                    break;
                    }
                default: {
                    var type = CONTENT_TYPES.get("text");
                    this.sendHeader(output, 403, HTTP_MESSAGE.FORBIDDEN_403, type, HTTP_MESSAGE.FORBIDDEN_403.length());

                    LogSystem.acces_log(Host, DTF.format(LocalDateTime.now()).toString(),method + " " +
                                    requestURL + "HTTP/1.1", 403,HTTP_MESSAGE.FORBIDDEN_403.length(),
                            requestURL, UserAgent);

                    output.write(HTTP_MESSAGE.FORBIDDEN_403.getBytes());
                    output.flush();
                    break;
                }
            }

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
        // var ps = new PrintStream(output);
        output.write(String.format("HTTP/1.1 %s %s%n", statusCode, statusText).getBytes(StandardCharsets.UTF_8));
        output.write(String.format("Date: %s%n", DTF.format(LocalDateTime.now())).getBytes(StandardCharsets.UTF_8)); ////////////////
        output.write(String.format("Content-Type: %s%n", type).getBytes(StandardCharsets.UTF_8));
        output.write(String.format("Content-Length: %s%n%n", lenght).getBytes(StandardCharsets.UTF_8));


        output.flush();
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