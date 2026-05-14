package programacionavanzada;

import java.io.*;
import java.net.*;
import java.util.Map;

public class ClienteHiloFerreteria extends Thread {

    private Socket socket;
    private BufferedReader entrada;
    private PrintWriter salida;
    private String nombre;

    public ClienteHiloFerreteria(Socket socket) throws IOException {
        this.socket = socket;

        entrada = new BufferedReader(
                new InputStreamReader(socket.getInputStream())
        );

        salida = new PrintWriter(
                socket.getOutputStream(), true
        );
    }

    @Override
    public void run() {
        try {
            salida.println("Ingrese su nombre:");
            nombre = entrada.readLine();

            ServidorFerreteria.log("[" + nombre + "] se conectó");
            salida.println("Bienvenido a la ferreteria " + nombre);

            String opcion;

            while (true) {
                menu();
                opcion = entrada.readLine();

                ServidorFerreteria.log("[" + nombre + "]: opción " + opcion);

                if (opcion.equals("3")) {
                    salida.println("Gracias por visitar la ferreteria");
                    break;
                }

                procesar(opcion);
            }

            socket.close();

        } catch (Exception e) {
            ServidorFerreteria.log((nombre != null ? nombre : "Cliente") + " desconectado");
        }
    }

    private void menu() {
        salida.println("MENU");
    }

    private void procesar(String opcion) {

        if (opcion.equals("1")) {
            String lista = "STOCK";
            for (Map.Entry<String, Integer> p : ServidorFerreteria.stock.entrySet()) {
                lista += "," + p.getKey() + "|" + ServidorFerreteria.precios.get(p.getKey()) + "|" + p.getValue();
            }
            salida.println(lista);
        }

        else if (opcion.equals("2")) {
            try {
                salida.println("PEDIR_PRODUCTO");
                String producto = entrada.readLine();

                salida.println("PEDIR_CANTIDAD");
                int cantidad = Integer.parseInt(entrada.readLine());

                if (!ServidorFerreteria.stock.containsKey(producto)) {
                    salida.println("ERROR|Producto no existe");
                    return;
                }

                int stockActual = ServidorFerreteria.stock.get(producto);
                double precio = ServidorFerreteria.precios.get(producto);

                if (stockActual < cantidad) {
                    salida.println("ERROR|Stock insuficiente");
                    return;
                }

                double total = precio * cantidad;
                salida.println("CONFIRMAR|" + producto + "|" + cantidad + "|" + total);

                String respuesta = entrada.readLine();

                if (respuesta.equalsIgnoreCase("si")) {
                    ServidorFerreteria.stock.put(producto, stockActual - cantidad);
                    salida.println("OK|Compra realizada con exito");
                    ServidorFerreteria.log("[" + nombre + "] compró " + cantidad + "x " + producto);
                } else {
                    salida.println("OK|Compra cancelada");
                }

            } catch (Exception e) {
                salida.println("ERROR|Error en la compra");
            }
        }

        else {
            salida.println("ERROR|Opcion no valida");
        }
    }
}