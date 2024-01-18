import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.Socket;

public class Client {
    private static final String fileName = "src/messages.txt";
    public static void main(String[] args) throws IOException {
        JFrame frame = new JFrame("Client Application");
        frame.getContentPane().setBackground(new Color(206, 204, 204));

        JTextField ipField = new JTextField(15);
        ipField.setBackground(new Color(250, 238, 209));
        ipField.setForeground(Color.BLACK);

        JTextField portField = new JTextField(5);
        portField.setBackground(new Color(250, 238, 209));
        portField.setForeground(Color.BLACK);

        JButton connectButton = new JButton("CONNECT");
        connectButton.setBackground(new Color(96, 114, 116));
        connectButton.setForeground(Color.WHITE);

        JTextArea textArea = new JTextArea(10, 30);
        textArea.setBackground(new Color(250, 238, 209));
        textArea.setForeground(Color.BLACK);
        textArea.setEditable(false);

        JScrollPane scrollPane = new JScrollPane(textArea, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        JTextField textField = new JTextField(30);
        textField.setBackground(new Color(250, 238, 209));
        textField.setForeground(Color.BLACK);

        JButton sendButton = new JButton("SEND");
        sendButton.setBackground(new Color(96, 114, 116));
        sendButton.setForeground(Color.WHITE);

        frame.setLayout(new BorderLayout());

        JPanel topPanel = new JPanel(new FlowLayout());
        topPanel.add(new JLabel("Server IP:"));
        topPanel.add(ipField);
        topPanel.add(new JLabel("Port:"));
        topPanel.add(portField);
        topPanel.add(connectButton);

        frame.add(topPanel, BorderLayout.NORTH);
        frame.add(new JScrollPane(textArea), BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.add(textField, BorderLayout.CENTER);
        bottomPanel.add(sendButton, BorderLayout.EAST);

        frame.add(bottomPanel, BorderLayout.SOUTH);

        frame.setSize(500, 500);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);


        connectButton.addActionListener(e -> {
            try {
                Socket socket = new Socket(ipField.getText(), Integer.parseInt(portField.getText()));
                DataInputStream dis = new DataInputStream(socket.getInputStream());
                DataOutputStream dos = new DataOutputStream(socket.getOutputStream());

                Thread inputThread = new Thread(() -> handleInput(dis, textArea));
                Thread outputThread = new Thread(() -> handleOutput(dos, textField, sendButton, textArea));

                inputThread.start();
                outputThread.start();
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        });
    }

    private static void handleInput(DataInputStream dis, JTextArea textArea) {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(fileName, true));

            String receivedMessage;
            do {
                receivedMessage = dis.readUTF();
                String decrypText = Encryption.Decrypt(receivedMessage);

                SwingUtilities.invokeLater(() -> textArea.append("Server says: " + decrypText + "\n"));
                //System.out.println("Encrypted and decrypted: " + Encryption.Encrypt(decrypText) + Encryption.Decrypt(receivedMessage));
            } while (!receivedMessage.equals("end"));

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void handleOutput(DataOutputStream dos, JTextField textField, JButton sendButton, JTextArea textArea) {
        sendButton.addActionListener(e -> {
            try {
                String messageToSend = textField.getText();
                String encryptText = Encryption.Encrypt(messageToSend);
                textArea.append("Client says: " + messageToSend + "\n");

                dos.writeUTF(encryptText);

            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        });

        textField.addActionListener(e -> {
            String messageToSend = textField.getText();
            if (!messageToSend.isEmpty()) {
                try {
                    String encryptText = Encryption.Encrypt(messageToSend);

                    dos.writeUTF(encryptText);
                    textArea.append("Client says: " + messageToSend + "\n");
                    textField.setText("");
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
            }
        });
    }
}