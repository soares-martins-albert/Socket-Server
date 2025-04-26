package socketclient.service;

import socketclient.view.Chat;
import socketclient.view.Login;

import javax.swing.*;
import java.io.*;
import java.net.Socket;

public class LoginService {

    private final Login loginFrame;
    private Socket socket;
    private BufferedReader reader;
    private PrintWriter writer;

    public LoginService(Login loginFrame) {
        this.loginFrame = loginFrame;
    }

    public void connect() {
        
        String userName = Login.getTxtUserName().getText().trim();
        String userIP = Login.getTxtUserIP().getText().trim();
        String userPort = Login.getTxtUserPort().getText().trim();
        String serverIP = Login.getTxtServerIP().getText().trim();
        String serverPortStr = Login.getTxtServerPort().getText().trim();

        if (userName.isEmpty() || userIP.isEmpty() || userPort.isEmpty() || serverIP.isEmpty() || serverPortStr.isEmpty()) {
            JOptionPane.showMessageDialog(loginFrame, "Todos os campos devem ser preenchidos.", "Erro de Validação", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int serverPort;
        try {
            serverPort = Integer.parseInt(serverPortStr);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(loginFrame, "A porta do servidor deve ser um número válido.", "Erro de Validação", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            socket = new Socket(serverIP, serverPort);
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            writer = new PrintWriter(socket.getOutputStream(), true);

            String mensagem = "/enter " + userName + " " + userIP + " " + userPort;
            writer.println(mensagem);

            String resposta = reader.readLine();
            if (resposta != null && resposta.equalsIgnoreCase("OK")) {
                JOptionPane.showMessageDialog(loginFrame, "Conectado com sucesso!", "Sucesso", JOptionPane.INFORMATION_MESSAGE);

                Chat chatFrame = new Chat();
                chatFrame.setVisible(true);
                loginFrame.dispose(); 
            } else {
                JOptionPane.showMessageDialog(loginFrame, "Falha na conexão: resposta inesperada do servidor.", "Erro", JOptionPane.ERROR_MESSAGE);
                socket.close();
            }

        } catch (IOException e) {
            JOptionPane.showMessageDialog(loginFrame, "Erro ao conectar no servidor: " + e.getMessage(), "Erro de Conexão", JOptionPane.ERROR_MESSAGE);
        }
    }
}
