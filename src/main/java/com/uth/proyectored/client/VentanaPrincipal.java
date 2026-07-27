package com.uth.proyectored.client;

import com.uth.proyectored.product.Producto;
import com.uth.proyectored.protocol.Mensaje;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.io.FileOutputStream;
import java.util.List;

public class VentanaPrincipal extends JFrame {

    private final ClienteConexion conexion = new ClienteConexion();

    private final JTextField campoHost = new JTextField("127.0.0.1", 14);
    private final JTextField campoPuerto = new JTextField("5000", 5);
    private final JButton botonConectar = new JButton("Conectar");

    private final DefaultTableModel modeloTabla =
            new DefaultTableModel(new Object[]{"ID", "Nombre", "Precio", "Stock"}, 0);
    private final JTable tablaProductos = new JTable(modeloTabla);

    private final JButton botonListar = new JButton("Listar productos");
    private final JButton botonAgregar = new JButton("Agregar producto");
    private final JButton botonReporte = new JButton("Generar reporte PDF");

    public VentanaPrincipal() {
        super("Proyecto en red - Cliente");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(560, 400);
        setLocationRelativeTo(null);
        construirInterfaz();
        habilitarAccionesRed(false);
    }

    private void construirInterfaz() {
        JPanel panelConexion = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panelConexion.add(new JLabel("Host/IP del servidor:"));
        panelConexion.add(campoHost);
        panelConexion.add(new JLabel("Puerto:"));
        panelConexion.add(campoPuerto);
        panelConexion.add(botonConectar);

        JPanel panelAcciones = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panelAcciones.add(botonListar);
        panelAcciones.add(botonAgregar);
        panelAcciones.add(botonReporte);

        setLayout(new BorderLayout());
        add(panelConexion, BorderLayout.NORTH);
        add(new JScrollPane(tablaProductos), BorderLayout.CENTER);
        add(panelAcciones, BorderLayout.SOUTH);

        botonConectar.addActionListener(e -> conectar());
        botonListar.addActionListener(e -> listarProductos());
        botonAgregar.addActionListener(e -> agregarProducto());
        botonReporte.addActionListener(e -> generarReporte());
    }

    private void habilitarAccionesRed(boolean habilitado) {
        botonListar.setEnabled(habilitado);
        botonAgregar.setEnabled(habilitado);
        botonReporte.setEnabled(habilitado);
    }

    private void conectar() {
        String host = campoHost.getText().trim();
        int puerto;
        try {
            puerto = Integer.parseInt(campoPuerto.getText().trim());
        } catch (NumberFormatException ex) {
            mostrarError("El puerto debe ser un numero");
            return;
        }

        try {
            conexion.conectar(host, puerto);
            habilitarAccionesRed(true);
            JOptionPane.showMessageDialog(this, "Conectado a " + host + ":" + puerto);
        } catch (Exception ex) {
            mostrarError("No se pudo conectar: " + ex.getMessage());
        }
    }

    private void listarProductos() {
        try {
            Mensaje respuesta = conexion.enviarYRecibir(new Mensaje(Mensaje.Tipo.LISTAR_PRODUCTOS));

            if (respuesta.getTipo() == Mensaje.Tipo.RESPUESTA_DATOS) {
                @SuppressWarnings("unchecked")
                List<Producto> productos = (List<Producto>) respuesta.getDato();

                modeloTabla.setRowCount(0);
                for (Producto p : productos) {
                    modeloTabla.addRow(new Object[]{p.getId(), p.getNombre(), p.getPrecio(), p.getStock()});
                }
            } else {
                mostrarError(respuesta.getTexto());
            }
        } catch (Exception ex) {
            mostrarError("Error al listar productos: " + ex.getMessage());
        }
    }

    private void agregarProducto() {
        JTextField nombre = new JTextField();
        JTextField precio = new JTextField();
        JTextField stock = new JTextField();

        Object[] campos = {"Nombre:", nombre, "Precio:", precio, "Stock:", stock};

        int opcion = JOptionPane.showConfirmDialog(this, campos, "Nuevo producto", JOptionPane.OK_CANCEL_OPTION);
        if (opcion != JOptionPane.OK_OPTION) return;

        try {
            Producto nuevo = new Producto(
                    0,
                    nombre.getText().trim(),
                    Double.parseDouble(precio.getText().trim()),
                    Integer.parseInt(stock.getText().trim())
            );

            Mensaje respuesta = conexion.enviarYRecibir(new Mensaje(Mensaje.Tipo.CREAR_PRODUCTO, nuevo));

            if (respuesta.getTipo() == Mensaje.Tipo.RESPUESTA_OK) {
                listarProductos();
            } else {
                mostrarError(respuesta.getTexto());
            }
        } catch (NumberFormatException nfe) {
            mostrarError("Precio y stock deben ser numeros");
        } catch (Exception ex) {
            mostrarError("Error al agregar producto: " + ex.getMessage());
        }
    }

    private void generarReporte() {
        try {
            Mensaje respuesta = conexion.enviarYRecibir(new Mensaje(Mensaje.Tipo.GENERAR_REPORTE));

            if (respuesta.getTipo() == Mensaje.Tipo.RESPUESTA_REPORTE) {
                byte[] pdf = (byte[]) respuesta.getDato();

                File archivo = File.createTempFile("reporte_productos_", ".pdf");
                try (FileOutputStream fos = new FileOutputStream(archivo)) {
                    fos.write(pdf);
                }

                if (Desktop.isDesktopSupported()) {
                    Desktop.getDesktop().open(archivo);
                } else {
                    JOptionPane.showMessageDialog(this, "PDF guardado en: " + archivo.getAbsolutePath());
                }
            } else {
                mostrarError(respuesta.getTexto());
            }
        } catch (Exception ex) {
            mostrarError("Error al generar el reporte: " + ex.getMessage());
        }
    }

    private void mostrarError(String texto) {
        JOptionPane.showMessageDialog(this, texto, "Error", JOptionPane.ERROR_MESSAGE);
    }
}
