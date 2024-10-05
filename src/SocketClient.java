import java.io.*;
import java.net.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.CountDownLatch;

public class SocketClient {
    public static void main(String[] args) {
        String host = "127.0.0.1";
        int port = 1145;

        try (Socket socket = new Socket(host, port);
             PrintWriter output = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             BufferedReader stdInput = new BufferedReader(new InputStreamReader(System.in))) {

            System.out.println("Connected to echo server at " + host + " on port " + port);

            AtomicBoolean canSend = new AtomicBoolean(false);
            CountDownLatch loginLatch = new CountDownLatch(1);

            // get server message thread
            Thread serverMessageThread = new Thread(() -> {
                try {
                    String serverMessage;
                    boolean isVerified = false;
                    while ((serverMessage = input.readLine()) != null) {
                        if ("Verify successful.".equals(serverMessage) && !isVerified) {
                            System.out.println("Logged in successfully.");
                            isVerified = true;
                            canSend.set(true);
                            loginLatch.countDown();
                        } else if (!isVerified) {
                            System.out.println("Login failed: " + serverMessage);
                            System.exit(0);
                        }
                    }
                } catch (IOException e) {
                    if (!socket.isClosed()) {
                        System.err.println("Error reading from server: " + e.getMessage());
                    }
                }
            });
            serverMessageThread.start();

            // verify
            System.out.println("Please enter your username:");
            String username = stdInput.readLine();
            System.out.println("Please enter your password:");
            String password = stdInput.readLine();

            // TODO 加密
            // send token
            output.println(username + "-" + password);

            loginLatch.await();

            if (canSend.get()) {
                System.out.println("Enter message to send");
                String userInput;
                while ((userInput = stdInput.readLine()) != null) {
                    // TODO 加密
                    output.println(userInput);
                    System.out.println("Send: " + userInput);
                }
            }

        } catch (UnknownHostException e) {
            System.err.println("Unknown host: " + host);
        } catch (IOException e) {
            System.err.println("I/O error: " + e.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("Thread was interrupted: " + e.getMessage());
        }
    }
}
