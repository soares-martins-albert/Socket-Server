package socketclient.service;

import socketclient.view.Login;

import javax.swing.*;
import java.io.*;
import java.net.*;

public class LoginService {

    private final Login loginFrame;

    public LoginService(Login loginFrame) {
        this.loginFrame = loginFrame;
    }

    public Boolean connect() {
        
        String userName = Login.getTxtUserName().getText().trim();
        String userIP = Login.getTxtUserIP().getText().trim();
        String userPortStr = Login.getTxtUserPort().getText().trim();
        String serverIP = Login.getTxtServerIP().getText().trim();
        String serverPortStr = Login.getTxtServerPort().getText().trim();

        if(!validateFields(userName,userIP,userPortStr,serverIP,serverPortStr)){
            return false;
        }
        
        int serverPort = Integer.parseInt(serverPortStr);
        int userPort = Integer.parseInt(userPortStr);

        boolean sent = sendEnterMsg(serverIP, serverPort, userIP, userPort, userName);
        boolean connected = sent && awaitServerResponse(serverIP, userPort);
        
        if(connected){ toggleButtons(); }
        
        return connected;
    }
   
    public Boolean validateFields(String userName, String userIP, String userPortStr, 
                                  String serverIP, String serverPortStr){
        
        if (userName.isEmpty() || userIP.isEmpty() || userPortStr.isEmpty() || 
            serverIP.isEmpty() || serverPortStr.isEmpty()) {
            JOptionPane.showMessageDialog(loginFrame, "Todos os campos devem ser preenchidos.", "Erro de Validação", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        
        try {
            Integer.valueOf(serverPortStr);
            Integer.valueOf(userPortStr);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(loginFrame, "As portas informadas devem ser números válidos.", "Erro de Validação", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        
        if((!userPortStr.isEmpty() && Integer.valueOf(userPortStr) > 65535) || 
           (!serverPortStr.isEmpty() && Integer.valueOf(serverPortStr) > 65535)){
            
            JOptionPane.showMessageDialog(loginFrame, "As portas informadas devem ser números válidos.", "Erro de Validação", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        
        return true;
    }
    
    private Boolean sendEnterMsg(String serverIP, int serverPort, String userIP, int userPort, String userName) {
        
        var msg = userIP+"p"+userPort+" \\enter "+userName;

        try (Socket sendSocket = new Socket(serverIP, serverPort);
             var doutSend = new DataOutputStream(sendSocket.getOutputStream())) {

            doutSend.writeUTF(msg);
            doutSend.flush();
            
            return true;

        } catch (IOException e) {
            JOptionPane.showMessageDialog(loginFrame, "Erro na comunicação com o servidor.","Erro", JOptionPane.ERROR_MESSAGE);
            
            return false;
        }
    }
    
    public boolean awaitServerResponse(String serverIP, int userPort) {
        ServerSocket serverSocket;
        Socket clientSocket;

        try {
            serverSocket = new ServerSocket(userPort);
            serverSocket.setSoTimeout(30000);

            clientSocket = serverSocket.accept();
            InetAddress clientAddress = clientSocket.getInetAddress();
            String senderIP = clientAddress.getHostAddress();
            
            if(senderIP.equals(serverIP)){
                
                serverSocket.close();
                return true;
            }
            
            serverSocket.close();

        } catch (SocketTimeoutException e) {
            JOptionPane.showMessageDialog(loginFrame, "O servidor não respondeu.","Erro", JOptionPane.ERROR_MESSAGE);
            return false;
        } catch (IOException e) {
            return false;
        }
        
        return false;
    }
    
    public void toggleButtons(){
        
        if(Login.btnEnter.isEnabled()){
            
            Login.btnEnter.setEnabled(false);
            Login.txtUserName.setEnabled(false);
            Login.txtUserPort.setEnabled(false);
            Login.txtServerPort.setEnabled(false);
            Login.txtServerIP.setEnabled(false);
            
        }else{
            Login.btnEnter.setEnabled(true);
            Login.txtUserName.setEnabled(true);
            Login.txtUserPort.setEnabled(true);
            Login.txtServerPort.setEnabled(true);
            Login.txtServerIP.setEnabled(true);
        }
        
    }
}
