import java.net.*;
import java.io.*;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

public class SocketServer {
    private final CopyOnWriteArrayList<User> users = new CopyOnWriteArrayList<>();

    public static void main(String[] args) {
        int port = 1145;
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Server is listening on port " + port);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                new Thread(new SocketClientHandler(clientSocket)).start();
            }
        } catch (IOException e) {
            System.out.println("Exception caught when trying to listen on port " + port + " or listening for a connection");
            System.out.println(e.getMessage());
        }
    }

    public CopyOnWriteArrayList<User> getUsers() {
        return users;
    }
}

class SocketClientHandler implements Runnable {
    private final Socket clientSocket;
    private User user;

    public SocketClientHandler(Socket socket) {
        this.clientSocket = socket;
    }

    public void run() {
        try (
                PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()))
        ) {
            String inputLine;
            boolean isVerified;

            String token = in.readLine();
            this.user = new User(token,clientSocket);

            // verify
            if (Verify.check(user)) {
                isVerified = true;
                out.println("Verify successful.");
                System.out.printf("Add new user(%s) ip %s%n",user.getUsername(),user.getClientSocket().getInetAddress().getHostAddress().replace("/",""));
            } else {
                out.println("Verify failed.");
                isVerified = false;
                // lol
                clientSocket.close();
            }

            while (isVerified && (inputLine = in.readLine()) != null) {
                out.println(inputLine);
                System.out.printf("%s(%s) Echoed： %s%n",user.getUsername(),user.getClientSocket().getInetAddress().getHostAddress().replace("/",""),inputLine);
            }
        } catch (IOException e) {
            System.out.printf("User %s is disconnected.%n",user.getUsername());
        }
    }

    public User getUser() {
        return user;
    }
}

class User {
    private String username;
    private String password;
    private Socket clientSocket;
    private PrintWriter out;

    public User(String verifyToken,Socket clientSocket) throws IOException {
        // TODO 解密
        String[] token = verifyToken.split("-");
        if (token[0] == null || token[1] == null) return;
        this.username = token[0];
        this.password = token[1];
        this.clientSocket = clientSocket;
        this.out = new PrintWriter(clientSocket.getOutputStream(), true);
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public Socket getClientSocket() {
        return clientSocket;
    }

    public PrintWriter getOut() {
        return out;
    }
}

class Verify {
    /*
    public static boolean check(User user) {
        String token = user.getPassword();
        String webContext = WebUtil.get("http://example.com/verify/" + user.getUsername() + ".txt");
        return Objects.equals(webContext, token);
    }
     */
    public static boolean check(User user) {
        return true;
    }
}

