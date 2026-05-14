package programacionavanzada;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.*;
import java.net.*;

public class ClienteFerreteria {

    private static PrintWriter salida;
    private static BufferedReader entrada;
    private static Socket socket;

    private static JFrame frame;
    private static JPanel panelNombre;
    private static JPanel panelMenu;
    private static JPanel panelStock;
    private static JPanel panelCompra;
    private static JLabel lblBienvenida;

    // Panel nombre
    private static JTextField txtNombre;

    // Panel stock
    private static DefaultTableModel modeloTabla;

    // Panel compra
    private static JComboBox<String> comboProducto;
    private static JSpinner spinnerCantidad;
    private static JLabel lblTotal;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(ClienteFerreteria::iniciarVentana);
    }

    // ──────────────────────────────────────────
    //  CONSTRUCCIÓN DE LA VENTANA
    // ──────────────────────────────────────────
    private static void iniciarVentana() {

        frame = new JFrame("Ferretería - Cliente");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(520, 420);
        frame.setLocationRelativeTo(null);
        frame.setLayout(new BorderLayout());

        lblBienvenida = new JLabel("Conectando...", JLabel.CENTER);
        lblBienvenida.setFont(new Font("SansSerif", Font.BOLD, 15));
        lblBienvenida.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        frame.add(lblBienvenida, BorderLayout.NORTH);

        // Construir todos los paneles
        construirPanelNombre();
        construirPanelMenu();
        construirPanelStock();
        construirPanelCompra();

        frame.add(panelNombre, BorderLayout.CENTER);
        frame.setVisible(true);

        // Conectar al servidor en hilo aparte
        new Thread(ClienteFerreteria::conectar).start();
    }

    // ──────────────────────────────────────────
    //  PANEL: INGRESAR NOMBRE
    // ──────────────────────────────────────────
    private static void construirPanelNombre() {
        panelNombre = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);

        JLabel lbl = new JLabel("Ingrese su nombre:");
        lbl.setFont(new Font("SansSerif", Font.PLAIN, 14));
        txtNombre = new JTextField(18);
        txtNombre.setFont(new Font("SansSerif", Font.PLAIN, 14));
        JButton btnEntrar = new JButton("Entrar");
        btnEntrar.setFont(new Font("SansSerif", Font.BOLD, 13));

        btnEntrar.addActionListener(e -> enviarNombre());
        txtNombre.addActionListener(e -> enviarNombre());

        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        panelNombre.add(lbl, gbc);
        gbc.gridy = 1;
        panelNombre.add(txtNombre, gbc);
        gbc.gridy = 2; gbc.gridwidth = 1;
        panelNombre.add(btnEntrar, gbc);
    }

    // ──────────────────────────────────────────
    //  PANEL: MENÚ PRINCIPAL
    // ──────────────────────────────────────────
    private static void construirPanelMenu() {
        panelMenu = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(12, 12, 12, 12);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.ipadx = 80; gbc.ipady = 15;

        JButton btnStock   = new JButton("📦  Ver Stock");
        JButton btnComprar = new JButton("🛒  Comprar Producto");
        JButton btnSalir   = new JButton("🚪  Salir");

        btnStock.setFont(new Font("SansSerif", Font.PLAIN, 14));
        btnComprar.setFont(new Font("SansSerif", Font.PLAIN, 14));
        btnSalir.setFont(new Font("SansSerif", Font.PLAIN, 14));

        btnStock.addActionListener(e -> pedirStock());
        btnComprar.addActionListener(e -> mostrarPanel(panelCompra));
        btnSalir.addActionListener(e -> salir());

        gbc.gridx = 0; gbc.gridy = 0; panelMenu.add(btnStock, gbc);
        gbc.gridy = 1;                 panelMenu.add(btnComprar, gbc);
        gbc.gridy = 2;                 panelMenu.add(btnSalir, gbc);
    }

    // ──────────────────────────────────────────
    //  PANEL: STOCK (tabla)
    // ──────────────────────────────────────────
    private static void construirPanelStock() {
        panelStock = new JPanel(new BorderLayout(8, 8));
        panelStock.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        modeloTabla = new DefaultTableModel(
                new String[]{"Producto", "Precio ($)", "Stock"}, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };

        JTable tabla = new JTable(modeloTabla);
        tabla.setFont(new Font("SansSerif", Font.PLAIN, 13));
        tabla.setRowHeight(24);
        tabla.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 13));

        JButton btnVolver = new JButton("← Volver al menú");
        btnVolver.addActionListener(e -> mostrarPanel(panelMenu));

        panelStock.add(new JScrollPane(tabla), BorderLayout.CENTER);
        panelStock.add(btnVolver, BorderLayout.SOUTH);
    }

    // ──────────────────────────────────────────
    //  PANEL: COMPRAR
    // ──────────────────────────────────────────
    private static void construirPanelCompra() {
        panelCompra = new JPanel(new GridBagLayout());
        panelCompra.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.anchor = GridBagConstraints.WEST;

        comboProducto  = new JComboBox<>(new String[]{"martillo","clavos","destornillador","taladro"});
        spinnerCantidad = new JSpinner(new SpinnerNumberModel(1, 1, 100, 1));
        lblTotal        = new JLabel("Total: -");
        lblTotal.setFont(new Font("SansSerif", Font.BOLD, 14));

        JButton btnCalcular = new JButton("Calcular total");
        JButton btnConfirmar = new JButton("Confirmar compra");
        JButton btnVolver   = new JButton("← Volver");

        btnCalcular.addActionListener(e -> calcularTotal());
        btnConfirmar.addActionListener(e -> confirmarCompra());
        btnVolver.addActionListener(e -> mostrarPanel(panelMenu));

        gbc.gridx=0; gbc.gridy=0; panelCompra.add(new JLabel("Producto:"), gbc);
        gbc.gridx=1;              panelCompra.add(comboProducto, gbc);
        gbc.gridx=0; gbc.gridy=1; panelCompra.add(new JLabel("Cantidad:"), gbc);
        gbc.gridx=1;              panelCompra.add(spinnerCantidad, gbc);
        gbc.gridx=0; gbc.gridy=2; panelCompra.add(btnCalcular, gbc);
        gbc.gridx=1;              panelCompra.add(lblTotal, gbc);
        gbc.gridx=0; gbc.gridy=3; gbc.gridwidth=2;
        panelCompra.add(btnConfirmar, gbc);
        gbc.gridy=4;
        panelCompra.add(btnVolver, gbc);
    }

    // ──────────────────────────────────────────
    //  CONEXIÓN Y LÓGICA DE MENSAJES
    // ──────────────────────────────────────────
    private static void conectar() {
        try {
            socket  = new Socket("localhost", 5004);
            entrada = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            salida  = new PrintWriter(socket.getOutputStream(), true);

            SwingUtilities.invokeLater(() ->
                    lblBienvenida.setText("Conectado al servidor ✓"));

            // Esperar el mensaje "Ingrese su nombre:"
            String msg = entrada.readLine();
            if (msg != null && msg.contains("nombre")) {
                SwingUtilities.invokeLater(() ->
                        lblBienvenida.setText("Por favor, ingrese su nombre"));
            }

        } catch (Exception e) {
            SwingUtilities.invokeLater(() ->
                    JOptionPane.showMessageDialog(frame,
                            "No se pudo conectar al servidor.\nVerifique que esté iniciado.",
                            "Error de conexión", JOptionPane.ERROR_MESSAGE));
        }
    }

    private static void enviarNombre() {
        String nombre = txtNombre.getText().trim();
        if (nombre.isEmpty()) return;

        salida.println(nombre);

        new Thread(() -> {
            try {
                String bienvenida = entrada.readLine(); // "Bienvenido..."
                SwingUtilities.invokeLater(() -> {
                    lblBienvenida.setText(bienvenida);
                    mostrarPanel(panelMenu);
                });
                entrada.readLine(); // consumir el MENU que manda el servidor
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private static void pedirStock() {
        salida.println("1");

        new Thread(() -> {
            try {
                String resp = entrada.readLine(); // "STOCK,martillo|2000.0|10,..."

                if (resp != null && resp.startsWith("STOCK")) {
                    String[] partes = resp.split(",");
                    SwingUtilities.invokeLater(() -> {
                        modeloTabla.setRowCount(0);
                        for (int i = 1; i < partes.length; i++) {
                            String[] datos = partes[i].split("\\|");
                            modeloTabla.addRow(new Object[]{
                                    datos[0],
                                    "$ " + datos[1],
                                    datos[2]
                            });
                        }
                        mostrarPanel(panelStock);
                    });
                }

                entrada.readLine(); // consumir MENU
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private static void calcularTotal() {
        String producto  = (String) comboProducto.getSelectedItem();
        int cantidad     = (int) spinnerCantidad.getValue();
        salida.println("2");

        new Thread(() -> {
            try {
                entrada.readLine(); // "PEDIR_PRODUCTO"
                salida.println(producto);

                entrada.readLine(); // "PEDIR_CANTIDAD"
                salida.println(String.valueOf(cantidad));

                String resp = entrada.readLine(); // CONFIRMAR|... o ERROR|...

                SwingUtilities.invokeLater(() -> {
                    if (resp.startsWith("CONFIRMAR")) {
                        String[] p = resp.split("\\|");
                        lblTotal.setText("Total: $" + p[3]);
                        // guardar estado para confirmarCompra sin reenviar
                        frame.getRootPane().putClientProperty("pendingResp", resp);
                    } else {
                        String[] p = resp.split("\\|");
                        JOptionPane.showMessageDialog(frame, p[1], "Aviso",
                                JOptionPane.WARNING_MESSAGE);
                        lblTotal.setText("Total: -");
                        // leer MENU del servidor
                        new Thread(() -> { try { entrada.readLine(); } catch(Exception ignored){} }).start();
                    }
                });

            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private static void confirmarCompra() {
        String pendiente = (String) frame.getRootPane().getClientProperty("pendingResp");
        if (pendiente == null || !pendiente.startsWith("CONFIRMAR")) {
            JOptionPane.showMessageDialog(frame,
                    "Primero calculá el total.", "Aviso", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int opcion = JOptionPane.showConfirmDialog(frame,
                "¿Confirmar la compra?\n" + lblTotal.getText(),
                "Confirmar", JOptionPane.YES_NO_OPTION);

        salida.println(opcion == JOptionPane.YES_OPTION ? "si" : "no");

        new Thread(() -> {
            try {
                String resp = entrada.readLine(); // OK|...
                String[] p = resp.split("\\|");
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(frame, p[1], "Resultado",
                            JOptionPane.INFORMATION_MESSAGE);
                    lblTotal.setText("Total: -");
                    frame.getRootPane().putClientProperty("pendingResp", null);
                    mostrarPanel(panelMenu);
                });
                entrada.readLine(); // consumir MENU
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private static void salir() {
        salida.println("3");
        try { socket.close(); } catch (Exception ignored) {}
        System.exit(0);
    }

    // ──────────────────────────────────────────
    //  UTILIDAD: cambiar panel central
    // ──────────────────────────────────────────
    private static void mostrarPanel(JPanel panel) {
        frame.getContentPane().remove(1); // quita el panel central actual
        frame.getContentPane().add(panel, BorderLayout.CENTER);
        frame.revalidate();
        frame.repaint();
    }
}              "No se pudo conectar al servidor.\nVerifique que esté iniciado.",