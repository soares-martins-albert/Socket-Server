/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package socketserver.model;

import javax.swing.table.AbstractTableModel;
import java.util.*;
import socketserver.entities.Client;

public class ModelClient extends AbstractTableModel {

    private final List<Client> clients;
    private final Map<String, Client> clientsHashMap;

    private final String[] columnNames = {"IP", "Nome", "Porta"};

    public ModelClient() {
        this.clients = new LinkedList<>();
        this.clientsHashMap = new HashMap<>();
    }

    public synchronized void addClient(Client client) {
        String key = client.getIp() + "p" + client.getPort(); 
        if (!clientsHashMap.containsKey(key)) {
            clients.add(client);
            clientsHashMap.put(key, client); 
            fireTableRowsInserted(clients.size() - 1, clients.size() - 1);
        }
    }

    public synchronized void removeClientByIpPort(String ip) {
        Client client = clientsHashMap.remove(ip);
        if (client != null) {
            int index = clients.indexOf(client);
            clients.remove(index);
            
            fireTableRowsDeleted(index, index);
        }
    }

    public Client getClientByIpPort(String ipPort) {
        return clientsHashMap.get(ipPort);
    }

    public boolean containsClient(String ip) {
        return clientsHashMap.containsKey(ip);
    }
    
    public List getClientsList(){
        return clients;
    }
    
    public Client getClientAt(int index){
        return clients.get(index);
    }

    @Override
    public int getRowCount() {
        return clients.size();
    }

    @Override
    public int getColumnCount() {
        return columnNames.length;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Client client = clients.get(rowIndex);
        return switch (columnIndex) {
            case 0 -> client.getIp();
            case 1 -> client.getName();
            case 2 -> client.getPort();
            default -> null;
        };
    }

    @Override
    public String getColumnName(int column) {
        return columnNames[column];
    }
}
