import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

class MessageSender implements Runnable {
    public final static int PORT = 2020; //puerto asignado al server
    private DatagramSocket socket;
    private String hostName;
    private ClientWindow window; //ventana que usamos para el chat e ingreso de ip

    MessageSender(DatagramSocket sock, String host, ClientWindow win) {
        socket = sock;
        hostName = host;
        window = win;
    }

    private void sendMessage(String s) throws Exception {
        byte buffer[] = s.getBytes(); //convierte el mensaje a bytes
        InetAddress address = InetAddress.getByName(hostName); //obtiene ip
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length, address, PORT);
        socket.send(packet); //crea y envia el paquete por el socket
    }

    public void run() {
        boolean connected = false;
        do {
            try {
                sendMessage("Nuevo cliente conectado - Bienvenido!");
                connected = true;
            } catch (Exception e) {
                window.displayMessage(e.getMessage());
            } //conecta al cliente y entra en un bucle infinito
        } while (!connected);
        while (true) {
            try {
                while (!window.message_is_ready) { //este bucle se repite infinitas veces
                    //esperando que esta condicion sea true (pq esta inicializado en false)
                    Thread.sleep(100);
                }
                sendMessage(window.getMessage()); //cuando es true manda en mensaje a la ventana
                window.setMessageReady(false); //vuelve a setearlo en false para esperar otro mensaje
            } catch (Exception e) {
                window.displayMessage(e.getMessage()); //mensaje de error

            }
        }
    }
}

class MessageReceiver implements Runnable {
    DatagramSocket socket;
    byte buffer[];
    ClientWindow window;

    MessageReceiver(DatagramSocket sock, ClientWindow win) {
        socket = sock;
        buffer = new byte[1024];
        window = win;
    }

    public void run() {
        while (true) {
            try { //bucle infinito que recibe paquetes
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);
                String received = new String(packet.getData(), 1, packet.getLength() - 1).trim();
                //crea una string con los datos recibidos
                String receivedFinal=" ";
                String senderIp="";
                boolean status=false;
                for (int i=0;i<received.length();i++){
                    if (received.charAt(i)=='#'){
                        status=true;
                    }
                    if (status==false){
                        senderIp=senderIp+received.charAt(i);
                    }
                    else {
                        receivedFinal=receivedFinal+received.charAt(i);
                    }
                }
                if (receivedFinal.equals(" ")){
                    receivedFinal="No";
                }
                System.out.println(receivedFinal);
                window.displayMessage(receivedFinal); //tmb se imprime en la ventana
            } catch (Exception e) {
                System.err.println(e);
            }
        }
    }
}

public class ChatClient {

    public static void main(String args[]) throws Exception {
        ClientWindow window = new ClientWindow();
        String host = window.getHostName();
        window.setTitle("UDP CHAT  Server: " + host);
        DatagramSocket socket = new DatagramSocket();
        MessageReceiver receiver = new MessageReceiver(socket, window);
        MessageSender sender = new MessageSender(socket, host, window);
        Thread receiverThread = new Thread(receiver); //thread para recibir mensajes asignado a la clase receiver
        Thread senderThread = new Thread(sender); //thread para mandar mensajes asignado a la clase sender
        // los threads se utilizan para poder realizar multiples aciones al mismo tiempo
        // por eso minetras que estoy escribiendo puedo estar ecibiendo msjs y viceversa
        receiverThread.start(); // inicio ambos threads
        senderThread.start();
    }
}