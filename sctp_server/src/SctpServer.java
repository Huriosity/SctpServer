import dao.GameDAO;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class SctpServer {
    private int port;
    private String directory;
    private static GameDAO gameDAO;

    private SctpServer(int port, String directory) {
        this.port = port;
        this.directory = directory;
    }

    private void start(){
        try (ServerSocket server = new ServerSocket(this.port)){
            while(true) {
                Socket socket = server.accept();
                Thread thread = new SctpServerHandler(socket, this.directory, gameDAO);
                thread.start();
            }
        } catch(IOException e){
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        gameDAO = new GameDAO();
        gameDAO.connect();
        new SctpServer(8080, "src/view").start();
    }
}











































































