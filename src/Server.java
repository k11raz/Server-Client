import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class Server {

    private static final String fileName = "src\\messages.txt";
    private static ArrayList<ClientHandler> clientHandlers = new ArrayList<>();

    public static void main(String[] args) throws IOException {
        JFrame frame = new JFrame("Server Side");
        frame.getContentPane().setBackground(new Color(206, 204, 204)); // Set background color to dark gray

        JTextField portField = new JTextField(5);
        portField.setBackground(new Color(250, 238, 209));
        portField.setForeground(Color.BLACK);

        JButton startButton = new JButton("Start Server");
        startButton.setBackground(new Color(96, 114, 116));
        startButton.setForeground(Color.WHITE);

        JTextArea textArea = new JTextArea(10, 30);
        textArea.setBackground(new Color(250, 238, 209));
        textArea.setEditable(false);
        textArea.setForeground(Color.BLACK);

        JTextField textField = new JTextField(30);
        textField.setBackground(new Color(250, 238, 209));
        textField.setForeground(Color.BLACK);

        JButton sendButton = new JButton("Send");
        sendButton.setBackground(new Color(96, 114, 116));
        sendButton.setForeground(Color.WHITE);

        frame.setLayout(new BorderLayout());

        JPanel topPanel = new JPanel(new FlowLayout());
        topPanel.add(new JLabel("Port Number to Listen:"));
        topPanel.add(portField);
        topPanel.add(startButton);

        frame.add(topPanel, BorderLayout.NORTH);
        frame.add(new JScrollPane(textArea), BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.add(textField, BorderLayout.CENTER);
        bottomPanel.add(sendButton, BorderLayout.EAST);

        frame.add(bottomPanel, BorderLayout.SOUTH);

        frame.setSize(500, 500);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);


        startButton.addActionListener(e -> {
            new Thread(() -> {
                try {
                    ServerSocket serverSocket = new ServerSocket(Integer.parseInt(portField.getText()));

                    textArea.append("Server is opened\n");
                    while (true) {
                        Socket socket = serverSocket.accept();
                        Thread clientThread = new Thread(new ClientHandler(socket, textArea));
                        clientThread.start();
                    }
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
            }).start();
        });

        textField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    sendMessage(textField, textArea);
                }
            }
        });

        sendButton.addActionListener(e -> sendMessage(textField, textArea));
    }

    private static void sendMessage(JTextField textField, JTextArea textArea) {
        String messageToSend = textField.getText();
        if (!messageToSend.isEmpty()) {
            // Format the message to be sent
            String formattedMessage = String.format("[%s][Server] [%s]\n",
                    getCurrentTimestamp(), messageToSend);

            String encrypText = Encryption.Encrypt(messageToSend);
            // System.out.println("Encrypted Message: " + encrypText);

            // Display the formatted message in the text area
            textArea.append("Server says:" + messageToSend + "\n");

            // Clear the text field
            textField.setText("");

            // Send the formatted message to all connected clients
            for (ClientHandler clientHandler : clientHandlers) {
                try {
                    clientHandler.dos.writeUTF(encrypText);
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
            }

            // Write the formatted message to a file
            try {
                BufferedWriter writer = new BufferedWriter(new FileWriter(fileName, true));
                writer.write(formattedMessage);
                writer.flush();
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    static class ClientHandler implements Runnable {
        private final Socket socket;
        private final JTextArea textArea;
        private DataInputStream dis;
        private DataOutputStream dos;

        public ClientHandler(Socket socket, JTextArea textArea) {
            this.socket = socket;
            this.textArea = textArea;
            try {
                this.dis = new DataInputStream(socket.getInputStream());
                this.dos = new DataOutputStream(socket.getOutputStream());
                clientHandlers.add(this);

                textArea.append("Client has connected to the Server\n");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void run() {
            try {
                BufferedWriter writer = new BufferedWriter(new FileWriter(fileName, true));
                String receivedMessage;
                do {


                    receivedMessage = dis.readUTF();
                    String decryptText = Encryption.Decrypt(receivedMessage);

                    System.out.println("Decrypted Message: " + decryptText);
                    textArea.append("Client says: " + decryptText + "\n");

                    String formattedMessage = String.format("[%s][Client] [%s]\n",
                            getCurrentTimestamp(), decryptText);


                    writer.write(formattedMessage);

                    writer.flush();
                } while (!receivedMessage.equals("end"));

                writer.close();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static String getCurrentTimestamp() {
        return new SimpleDateFormat("dd.MM.yyyy HH:mm:ss").format(new Date());
    }
}