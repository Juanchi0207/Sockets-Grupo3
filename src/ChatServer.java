import java.io.*;
import java.net.*;
import java.util.*;

public class ChatServer implements  Runnable {
    public final static int PORT = 2020;
    private final static int BUFFER = 1024;

    private DatagramSocket socket;
    private ArrayList<InetAddress> client_addresses;
    private ArrayList<Integer> client_ports; //puertos de los cliente
    private HashSet<String> existing_clients; //hashset que contiene a todos los clientes conectados

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public ChatServer() throws IOException {
        socket = new DatagramSocket(PORT);
        System.out.println("Server is running and is listening on port " + PORT);
        client_addresses = new ArrayList();
        client_ports = new ArrayList();
        existing_clients = new HashSet();
    }

    public void run() {
        byte[] buffer = new byte[BUFFER]; //buffer en el que se almacenan los datos recibidos por el socket
        while (true) {
            try {
                Arrays.fill(buffer, (byte) 0);
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);

                String message = new String(buffer, 0, buffer.length);

                InetAddress clientAddress = packet.getAddress(); //de la clase DatagramPacket usamos
                //metodo para obtener la ip de ese paquete
                int client_port = packet.getPort(); //lo mismo para el puerto asignado a esta comunicacion
                String id = clientAddress.toString() + "|" + client_port;
                // si el cliente no estaba ya conectado se lo agrega a:
                if (!existing_clients.contains(id)) {
                    existing_clients.add(id); //su string convertida con la ip y el puerto en el hashset
                    client_ports.add(client_port); // el puerto por separado en un array
                    client_addresses.add(clientAddress); //la ip por separado en otro array
                }

                String received=id + " :" + message;
                System.out.println(received); //muestra el msj por consola
                String senderIp="/";
                boolean status=false;
                for (int i=0;i<received.length();i++){
                    if (received.charAt(i)==':'){
                        status=true;
                        i++;
                    }
                    if (received.charAt(i) == '#'){
                        status=false;
                    }
                    if (status){
                        senderIp=senderIp+received.charAt(i);
                    }
                }
                byte[] data = (id + " :" + message).getBytes(); //guardamos los datos obtenidos en el byte
                for (int i = 0; i < client_addresses.size(); i++) {
                    InetAddress cl_address = client_addresses.get(i); //ACA HAY QUE CAMBIAR QUE SEA SOLO AL QUE TIENE LA IP ESPECIFICADA
                    int cl_port = client_ports.get(i);
                    System.out.println(senderIp + "   sender");
                    System.out.println(client_addresses.get(i)+ "    lista");
                    if (client_addresses.get(i).toString().equals(senderIp)){
                        packet = new DatagramPacket(data, data.length, cl_address, cl_port);
                        socket.send(packet);
                    }
                }
            } catch (Exception e) {
                System.err.println(e);
            }
        }
    }

    public DatagramSocket getSocket() {
        return socket;
    }

    public void setSocket(DatagramSocket socket) {
        this.socket = socket;
    }

    public ArrayList<InetAddress> getClient_addresses() {
        return client_addresses;
    }

    public void setClient_addresses(ArrayList<InetAddress> client_addresses) {
        this.client_addresses = client_addresses;
    }

    public ArrayList<Integer> getClient_ports() {
        return client_ports;
    }

    public void setClient_ports(ArrayList<Integer> client_ports) {
        this.client_ports = client_ports;
    }

    public HashSet<String> getExisting_clients() {
        return existing_clients;
    }

    public void setExisting_clients(HashSet<String> existing_clients) {
        this.existing_clients = existing_clients;
    }

    public static void main(String args[]) throws Exception {
        ChatServer server_thread = new ChatServer();
        server_thread.run();
    }
}