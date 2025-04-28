/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package socketserver.service;

import socketserver.entities.Client;
import socketserver.model.ModelClient;
import socketserver.view.Server;

import java.io.*;
import java.net.*;
import javax.swing.*;
import java.time.LocalTime;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import socketserver.view.Connections;

public class ServerService {
    
    private final Server serverFrame;
    private final ModelClient model; 

    public ServerService(Server serverFrame, ModelClient model) {
        this.serverFrame = serverFrame;
        this.model = model;
    }
    
    public boolean start(){
        
        String srvIP = Server.getTxtServerIP().getText().trim();
        String srvPortStr = Server.getTxtServerPort().getText().trim();
        
        if(!validateFields(srvIP, srvPortStr)){
            return false;
        }
        
        int srvPort = Integer.valueOf(srvPortStr);
        
        startListening(srvPort, srvIP);
        
        toggleButtons();
        
        return true;
    }
    
    public Boolean validateFields(String srvIP, String srvPort){
        if(srvIP.isEmpty() || srvPort.isEmpty()){
            JOptionPane.showMessageDialog(serverFrame, "Todos os campos devem ser preenchidos.", "Erro de Validação", JOptionPane.ERROR_MESSAGE);
            return false;   
        }
        
        try {
            Integer.valueOf(srvPort);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(serverFrame, "As portas informadas devem ser números válidos.", "Erro de Validação", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        
        if(Integer.valueOf(srvPort) > 65535){
            JOptionPane.showMessageDialog(serverFrame, "As portas informadas devem ser números válidos.", "Erro de Validação", JOptionPane.ERROR_MESSAGE);
            return false;            
        }
        
        return true;
    }
    
    private void startListening(int port, String ip) {
        new Thread(() -> {
            try (ServerSocket serverSocket = new ServerSocket(port)) {
                
                Client srv = new Client(ip, "Servidor", port);
                model.addClient(srv);
                
                while (true) {
                    Socket con = serverSocket.accept();
                    
                    new Thread(() -> {
                        try (DataInputStream dis = new DataInputStream(con.getInputStream())) {
                            String msg = dis.readUTF();
                            
                            System.out.println(msg);
                            
                            processMsg(msg);
                            
                            String clientKey = msg.split(" ")[0];
                            
                            Client sender = model.getClientByIpPort(clientKey);
                            
                            msg = formatMsg(sender, msg.split(" ",2)[1]);
                            
                            Server.getTxtLogServer().append("\n"+msg);
                            
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
    
    public void processMsg(String msg){
        if(!msg.isEmpty()){
            
            String[] splitedMsg = msg.split(" ");
            String clientKey = splitedMsg[0];
            String ip = clientKey.split("p")[0];
            String port = clientKey.split("p")[1];
            String name = splitedMsg[splitedMsg.length-1];
            
            if(splitedMsg[1].equalsIgnoreCase("\\Enter")){
                
                var clt = new Client(ip, name, Integer.valueOf(port));
                model.addClient(clt);
                
                System.out.println(model.getClientByIpPort(clientKey).toString());
                
                Client srv = model.getClientAt(0);
                
                sendMsg(clt, srv, "connection accepted");
                
                streamMsg(clt.getName()+" entrou", srv);
                
            } else {
                streamMsg(msg.split(" ",2)[1], model.getClientByIpPort(clientKey));
            }
        }
    }
    
    private void streamMsg(String msg, Client sender) {
        
        ExecutorService executorService = Executors.newFixedThreadPool(10);
        
        List<Client> clients = model.getClientsList();
        
        Client srv = model.getClientAt(0);
        
        for (Client client : clients) {
            if(!client.equals(srv)){
                System.out.println(client.getName());
                executorService.submit(() -> sendMsg(client, sender, msg));
            }
        }
        
        executorService.shutdown();
    }
    
    public void streamMsg(String msg) {
        
        ExecutorService executorService = Executors.newFixedThreadPool(10);
        
        List<Client> clients = model.getClientsList();
        
        Client srv = model.getClientAt(0);
        
        for (Client client : clients) {
            if(!client.equals(srv)){
                executorService.submit(() -> sendMsg(client, srv, msg));
            }
        }
        executorService.shutdown();
    }
    
    private void sendMsg(Client receiver, Client sender, String msg){
        
        if(msg.isEmpty()){
            JOptionPane.showMessageDialog(null, "Não é possível enviar mensagens em branco", "Validação", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        try (Socket sendSocket = new Socket(receiver.getIp(), receiver.getPort());
             var doutSend = new DataOutputStream(sendSocket.getOutputStream())) {
            
            doutSend.writeUTF(formatMsg(sender, msg));
            doutSend.flush();
            
            sendSocket.close();
        } catch (IOException e) {
            disconnectClient(receiver);
        }
    }
    
    private String formatMsg(Client sender, String msg){
        
        LocalTime time = LocalTime.now();
        int hour = time.getHour();
        int minute = time.getMinute();
        
        msg = sender.getName()+" ("+hour+":"+minute+"): "+msg;
        
        return msg;
    }
    
    private void disconnectClient(Client clt){
        Client srv = model.getClientAt(0);
            
        streamMsg(clt.getName()+" saiu" , srv);

        model.removeClientByIpPort(clt.getIp()+"p"+clt.getPort());
    }
    
    private void toggleButtons(){
        if(Server.btnStart.isEnabled()){
            
            Server.btnStart.setEnabled(false);
            
            Server.btnConnections.setEnabled(true);
            Server.txtMsgServer.setEnabled(true);
            Server.btnSend.setEnabled(true);
            
        }else{
            Server.btnStart.setEnabled(true);
            
            Server.btnConnections.setEnabled(false);
            Server.txtMsgServer.setEnabled(false);
            Server.btnSend.setEnabled(false);
        }
    } 
}
