package programacionavanzada;

import javax.swing.*;
import java.awt.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;

public class ServidorFerreteria {

    public static HashMap<String, Integer> stock = new HashMap<>();
    public static HashMap<String, Double> precios = new HashMap<>();
    public static JTextArea logArea;

    public static void main(String[] args) {

        int puerto = 5004;

        stock.put("martillo", 10);
        stock.put("clavos", 50);
        stock.put("destornillador", 15);
        stock.put("taladro", 5);

        precios.put("martillo", 2000.0);
        precios.put("clavos", 100.0);
        precios.put("destornillador", 1500.0);
        precios.put("taladro", 15000.0);

        // --- Ventana Swing del servidor ---
        JFrame frame = new JFrame("Servidor Ferretería");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(500, 400);
        frame.setLayout(new BorderLayout());

        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setFont(new Font("Monospaced", Font.PLAIN, 13));
        JScrollPane scroll = new JScrollPane(logArea);

        JLabel titulo = new JLabel("  Log del Servidor - Puerto " + puerto, JLabel.LEFT);
        titulo.setFont(new Font("SansSerif", Font.BOLD, 14));
        titulo.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        frame.add(titulo, BorderLayout.NORTH);
        frame.add(scroll, BorderLayout.CENTER);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        log("Servidor Ferretería iniciado en puerto " + puerto);

        try {
            ServerSocket servidor = new ServerSocket(puerto);

            while (true) {
                Socket socket = servidor.accept();
                log("Cliente conectado desde: " + socket.getInetAddress());
                new ClienteHiloFerreteria(socket).start();
            }

        } catch (Exception e) {
            log("Error: " + e.getMessage());
        }
    }

    public static void log(String mensaje) {
        SwingUtilities.invokeLater(() -> {
            logArea.append(mensaje + "\n");
            logArea.setCaretPosition(logArea.getDocument().getLength());
        });
    }
}