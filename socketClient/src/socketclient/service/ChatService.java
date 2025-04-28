/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package socketclient.service;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import socketclient.view.Chat;


public class ChatService {
    

    public ChatService() {
        
    }
    
    public void startListening(int port) {
        new Thread(() -> {
            try (ServerSocket serverSocket = new ServerSocket(port)) {
                
                while (true) {
                    Socket con = serverSocket.accept();
                    
                    new Thread(() -> {
                        try (DataInputStream dis = new DataInputStream(con.getInputStream())) {
                            String msg = dis.readUTF();
                            
                            Chat.txtChat.append("\n"+msg);
                            
                            con.close();
                        } catch (IOException e) {
                            SwingUtilities.invokeLater(() -> System.out.println("Erro ao ler dados: " + e.getMessage()));
                        } finally {
                            try {
                                con.close();
                            } catch (IOException e) {
                                SwingUtilities.invokeLater(() -> System.out.println("Erro ao fechar a conexão: " + e.getMessage()));
                            }
                        }
                    }).start();
                }
            } catch (IOException e) {
                SwingUtilities.invokeLater(() -> System.out.println("Erro: " + e));
            }
        }).start();
    }
    
    public Boolean sendMsg(String serverIP, int serverPort, String userIP, int userPort, String msg) {
        
        if(msg.isEmpty()){
            JOptionPane.showMessageDialog(null, "Não é possível enviar mensagens em branco", "Validação", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        
        try (Socket sendSocket = new Socket(serverIP, serverPort);
            var doutSend = new DataOutputStream(sendSocket.getOutputStream())) {

            doutSend.writeUTF(userIP+"p"+userPort+" "+msg);
            doutSend.flush();
            
            sendSocket.close();
            
            return true;

        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Erro na comunicação com o servidor.","Erro", JOptionPane.ERROR_MESSAGE);
            
            return false;
        }
    }
}
