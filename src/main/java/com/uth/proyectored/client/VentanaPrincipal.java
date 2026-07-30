package com.uth.proyectored.client;

import com.uth.proyectored.product.Producto;
import com.uth.proyectored.product.Venta;
import com.uth.proyectored.product.SolicitudVenta;
import com.uth.proyectored.product.FacturaGenerada;
import com.uth.proyectored.protocol.Mensaje;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.io.File;
import java.io.FileOutputStream;
import java.util.List;

public class VentanaPrincipal extends JFrame {

    
    private static final Color COLOR_PRIMARIO = new Color(0x2F6FED);
    private static final Color COLOR_VERDE = new Color(0x1E9E5A);
    private static final Color COLOR_ROJO = new Color(0xD64545);
    private static final Color COLOR_GRIS_TEXTO = new Color(0x555555);
    private static final Font FUENTE_BASE = new Font("Segoe UI", Font.PLAIN, 13);
    private static final Font FUENTE_TITULO = new Font("Segoe UI", Font.BOLD, 13);

    private final ClienteConexion conexion = new ClienteConexion();

    private final JTextField campoHost = new JTextField("127.0.0.1", 14);
    private final JTextField campoPuerto = new JTextField("5000", 5);
    private final JButton botonConectar = new JButton("Conectar");
    private final JLabel estadoConexion = new JLabel("\u25CF Desconectado");

    private final JTextField campoBusqueda = new JTextField();

    private final DefaultTableModel modeloTabla = new DefaultTableModel(
            new Object[]{"ID", "Nombre", "Precio", "Stock"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable tablaProductos = new JTable(modeloTabla);
    private final TableRowSorterCompat filtro = new TableRowSorterCompat(modeloTabla);

    private final JButton botonListar = new JButton("\u21BB Listar");
    private final JButton botonAgregar = new JButton("+ Agregar");
    private final JButton botonEditar = new JButton("\u270E Editar");
    private final JButton botonEliminar = new JButton("\u2716 Eliminar");
    private final JButton botonVender = new JButton("$ Vender");
    private final JButton botonHistorial = new JButton("\u2263 Historial ventas");

    private final JLabel etiquetaContador = new JLabel("0 productos");

    public VentanaPrincipal() {
        super("Proyecto en red - Cliente");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(760, 480);
        setMinimumSize(new Dimension(620, 400));
        setLocationRelativeTo(null);
        construirInterfaz();
        habilitarAccionesRed(false);
        habilitarAccionesFila(false);
    }

    private void construirInterfaz() {
        getContentPane().setLayout(new BorderLayout(0, 0));
        getContentPane().setBackground(Color.WHITE);

        add(construirPanelConexion(), BorderLayout.NORTH);
        add(construirPanelCentral(), BorderLayout.CENTER);
        add(construirPanelAcciones(), BorderLayout.SOUTH);

        botonConectar.addActionListener(e -> conectar());
        botonListar.addActionListener(e -> listarProductos());
        botonAgregar.addActionListener(e -> agregarProducto());
        botonEditar.addActionListener(e -> editarProducto());
        botonEliminar.addActionListener(e -> eliminarProducto());
        botonVender.addActionListener(e -> venderProducto());
        botonHistorial.addActionListener(e -> verHistorialVentas());

        tablaProductos.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                habilitarAccionesFila(tablaProductos.getSelectedRow() != -1);
            }
        });

        campoBusqueda.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { filtro.aplicar(campoBusqueda.getText()); }
            public void removeUpdate(DocumentEvent e) { filtro.aplicar(campoBusqueda.getText()); }
            public void changedUpdate(DocumentEvent e) { filtro.aplicar(campoBusqueda.getText()); }
        });
    }

    private JPanel construirPanelConexion() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new EmptyBorder(12, 16, 12, 16));
        panel.setBackground(new Color(0xF7F8FA));

        JPanel izquierda = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        izquierda.setOpaque(false);
        JLabel etiquetaHost = new JLabel("Servidor:");
        etiquetaHost.setFont(FUENTE_TITULO);
        izquierda.add(etiquetaHost);
        izquierda.add(campoHost);
        JLabel etiquetaPuerto = new JLabel("Puerto:");
        etiquetaPuerto.setFont(FUENTE_TITULO);
        izquierda.add(etiquetaPuerto);
        izquierda.add(campoPuerto);
        estilizarBotonPrimario(botonConectar);
        izquierda.add(botonConectar);

        estadoConexion.setFont(FUENTE_TITULO);
        estadoConexion.setForeground(COLOR_ROJO);

        panel.add(izquierda, BorderLayout.WEST);
        panel.add(estadoConexion, BorderLayout.EAST);
        return panel;
    }

    private JPanel construirPanelCentral() {
        JPanel panel = new JPanel(new BorderLayout(0, 8));
        panel.setBorder(new EmptyBorder(12, 16, 0, 16));

        JPanel panelBusqueda = new JPanel(new BorderLayout(8, 0));
        JLabel lupa = new JLabel("Buscar:");
        lupa.setFont(FUENTE_BASE);
        campoBusqueda.setFont(FUENTE_BASE);
        campoBusqueda.putClientProperty("JTextField.placeholderText", "Filtrar por nombre...");
        panelBusqueda.add(lupa, BorderLayout.WEST);
        panelBusqueda.add(campoBusqueda, BorderLayout.CENTER);

        estilizarTabla();
        JScrollPane scroll = new JScrollPane(tablaProductos);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(0xE0E0E0)));

        etiquetaContador.setFont(FUENTE_BASE);
        etiquetaContador.setForeground(COLOR_GRIS_TEXTO);

        panel.add(panelBusqueda, BorderLayout.NORTH);
        panel.add(scroll, BorderLayout.CENTER);
        panel.add(etiquetaContador, BorderLayout.SOUTH);
        return panel;
    }

    private void estilizarTabla() {
        tablaProductos.setFont(FUENTE_BASE);
        tablaProductos.setRowHeight(26);
        tablaProductos.setSelectionBackground(new Color(0xDCE8FF));
        tablaProductos.setSelectionForeground(Color.BLACK);
        tablaProductos.setGridColor(new Color(0xEDEDED));
        tablaProductos.setShowGrid(true);
        tablaProductos.setRowSorter(filtro.getSorter());

        JTableHeader header = tablaProductos.getTableHeader();
        header.setFont(FUENTE_TITULO);
        header.setBackground(new Color(0xF0F2F5));
        header.setPreferredSize(new Dimension(header.getWidth(), 30));

        tablaProductos.getColumnModel().getColumn(0).setPreferredWidth(50);
        tablaProductos.getColumnModel().getColumn(1).setPreferredWidth(260);
        tablaProductos.getColumnModel().getColumn(2).setPreferredWidth(100);
        tablaProductos.getColumnModel().getColumn(3).setPreferredWidth(80);
    }

    private JPanel construirPanelAcciones() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 10));
        panel.setBorder(new EmptyBorder(4, 12, 4, 12));
        panel.setBackground(new Color(0xF7F8FA));

        estilizarBotonSecundario(botonListar);
        estilizarBotonPrimario(botonAgregar);
        estilizarBotonSecundario(botonEditar);
        estilizarBotonPeligro(botonEliminar);
        estilizarBotonPrimario(botonVender);
        estilizarBotonSecundario(botonHistorial);

        panel.add(botonListar);
        panel.add(botonAgregar);
        panel.add(botonEditar);
        panel.add(botonEliminar);
        panel.add(botonVender);
        panel.add(botonHistorial);
        return panel;
    }

    private void estilizarBotonPrimario(JButton boton) {
        boton.setFont(FUENTE_TITULO);
        boton.setBackground(COLOR_PRIMARIO);
        boton.setForeground(Color.WHITE);
        boton.setFocusPainted(false);
        boton.setBorder(new EmptyBorder(6, 14, 6, 14));
    }

    private void estilizarBotonSecundario(JButton boton) {
        boton.setFont(FUENTE_BASE);
        boton.setFocusPainted(false);
        boton.setBorder(new EmptyBorder(6, 12, 6, 12));
    }

    private void estilizarBotonPeligro(JButton boton) {
        boton.setFont(FUENTE_BASE);
        boton.setForeground(COLOR_ROJO);
        boton.setFocusPainted(false);
        boton.setBorder(new EmptyBorder(6, 12, 6, 12));
    }

    private void habilitarAccionesRed(boolean habilitado) {
        botonListar.setEnabled(habilitado);
        botonAgregar.setEnabled(habilitado);
        botonHistorial.setEnabled(habilitado);
        if (!habilitado) habilitarAccionesFila(false);
    }

    private void habilitarAccionesFila(boolean habilitado) {
        botonEditar.setEnabled(habilitado);
        botonEliminar.setEnabled(habilitado);
        botonVender.setEnabled(habilitado);
    }

    private void actualizarEstadoConexion(boolean conectado) {
        if (conectado) {
            estadoConexion.setText("\u25CF Conectado");
            estadoConexion.setForeground(COLOR_VERDE);
        } else {
            estadoConexion.setText("\u25CF Desconectado");
            estadoConexion.setForeground(COLOR_ROJO);
        }
    }

    private void conectar() {
        String host = campoHost.getText().trim();
        if (host.isEmpty()) {
            mostrarError("Escribe el host o IP del servidor");
            return;
        }

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
            actualizarEstadoConexion(true);
            listarProductos();
        } catch (Exception ex) {
            actualizarEstadoConexion(false);
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
                etiquetaContador.setText(productos.size() + " producto" + (productos.size() == 1 ? "" : "s"));
                habilitarAccionesFila(false);
            } else {
                mostrarError(respuesta.getTexto());
            }
        } catch (Exception ex) {
            actualizarEstadoConexion(false);
            habilitarAccionesRed(false);
            mostrarError("Error al listar productos: " + ex.getMessage());
        }
    }

    private void agregarProducto() {
        Producto nuevo = mostrarDialogoProducto("Nuevo producto", null);
        if (nuevo == null) return;

        try {
            Mensaje respuesta = conexion.enviarYRecibir(new Mensaje(Mensaje.Tipo.CREAR_PRODUCTO, nuevo));
            if (respuesta.getTipo() == Mensaje.Tipo.RESPUESTA_OK) {
                listarProductos();
            } else {
                mostrarError(respuesta.getTexto());
            }
        } catch (Exception ex) {
            mostrarError("Error al agregar producto: " + ex.getMessage());
        }
    }

    private void editarProducto() {
        int fila = tablaProductos.getSelectedRow();
        if (fila == -1) return;
        int filaModelo = tablaProductos.convertRowIndexToModel(fila);

        Producto actual = new Producto(
                (Integer) modeloTabla.getValueAt(filaModelo, 0),
                (String) modeloTabla.getValueAt(filaModelo, 1),
                (Double) modeloTabla.getValueAt(filaModelo, 2),
                (Integer) modeloTabla.getValueAt(filaModelo, 3)
        );

        Producto editado = mostrarDialogoProducto("Editar producto", actual);
        if (editado == null) return;
        editado.setId(actual.getId());

        try {
            Mensaje respuesta = conexion.enviarYRecibir(new Mensaje(Mensaje.Tipo.EDITAR_PRODUCTO, editado));
            if (respuesta.getTipo() == Mensaje.Tipo.RESPUESTA_OK) {
                listarProductos();
            } else {
                mostrarError(respuesta.getTexto());
            }
        } catch (Exception ex) {
            mostrarError("Error al editar producto: " + ex.getMessage());
        }
    }

    private void eliminarProducto() {
        int fila = tablaProductos.getSelectedRow();
        if (fila == -1) return;
        int filaModelo = tablaProductos.convertRowIndexToModel(fila);

        int id = (Integer) modeloTabla.getValueAt(filaModelo, 0);
        String nombre = (String) modeloTabla.getValueAt(filaModelo, 1);

        int confirmacion = JOptionPane.showConfirmDialog(
                this,
                "¿Eliminar \"" + nombre + "\"? Esta accion no se puede deshacer.",
                "Confirmar eliminacion",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );
        if (confirmacion != JOptionPane.YES_OPTION) return;

        try {
            Mensaje respuesta = conexion.enviarYRecibir(new Mensaje(Mensaje.Tipo.ELIMINAR_PRODUCTO, id));
            if (respuesta.getTipo() == Mensaje.Tipo.RESPUESTA_OK) {
                listarProductos();
            } else {
                mostrarError(respuesta.getTexto());
            }
        } catch (Exception ex) {
            mostrarError("Error al eliminar producto: " + ex.getMessage());
        }
    }

    /**
     * Dialogo compartido para crear y editar. Si "existente" es null, es un
     * producto nuevo (campos vacios); si no, precarga los valores actuales.
     * Valida los campos ANTES de cerrar el dialogo, mostrando el error dentro
     * del mismo dialogo para que el usuario pueda corregir sin perder lo que
     * ya escribio.
     */
    private Producto mostrarDialogoProducto(String titulo, Producto existente) {
        JTextField campoNombre = new JTextField(existente != null ? existente.getNombre() : "");
        JTextField campoPrecio = new JTextField(existente != null ? String.valueOf(existente.getPrecio()) : "");
        JTextField campoStock = new JTextField(existente != null ? String.valueOf(existente.getStock()) : "");

        JPanel panel = new JPanel(new GridLayout(0, 1, 4, 4));
        panel.add(etiquetaCampo("Nombre:"));
        panel.add(campoNombre);
        panel.add(etiquetaCampo("Precio:"));
        panel.add(campoPrecio);
        panel.add(etiquetaCampo("Stock:"));
        panel.add(campoStock);

        while (true) {
            int opcion = JOptionPane.showConfirmDialog(this, panel, titulo, JOptionPane.OK_CANCEL_OPTION);
            if (opcion != JOptionPane.OK_OPTION) return null;

            String nombre = campoNombre.getText().trim();
            String textoPrecio = campoPrecio.getText().trim();
            String textoStock = campoStock.getText().trim();

            String errorValidacion = validarCampos(nombre, textoPrecio, textoStock);
            if (errorValidacion != null) {
                mostrarError(errorValidacion);
                continue;
            }

            double precio = Double.parseDouble(textoPrecio);
            int stock = Integer.parseInt(textoStock);
            return new Producto(0, nombre, precio, stock);
        }
    }

    private String validarCampos(String nombre, String textoPrecio, String textoStock) {
        if (nombre.isEmpty()) {
            return "El nombre no puede estar vacio";
        }
        if (nombre.length() > 100) {
            return "El nombre no puede tener mas de 100 caracteres";
        }

        double precio;
        try {
            precio = Double.parseDouble(textoPrecio);
        } catch (NumberFormatException ex) {
            return "El precio debe ser un numero (ej. 199.99)";
        }
        if (precio < 0) {
            return "El precio no puede ser negativo";
        }

        int stock;
        try {
            stock = Integer.parseInt(textoStock);
        } catch (NumberFormatException ex) {
            return "El stock debe ser un numero entero (ej. 10)";
        }
        if (stock < 0) {
            return "El stock no puede ser negativo";
        }

        return null;
    }

    private JLabel etiquetaCampo(String texto) {
        JLabel label = new JLabel(texto);
        label.setFont(FUENTE_TITULO);
        return label;
    }

    private void venderProducto() {
        int fila = tablaProductos.getSelectedRow();
        if (fila == -1) return;
        int filaModelo = tablaProductos.convertRowIndexToModel(fila);

        int productoId = (Integer) modeloTabla.getValueAt(filaModelo, 0);
        String nombre = (String) modeloTabla.getValueAt(filaModelo, 1);
        int stockDisponible = (Integer) modeloTabla.getValueAt(filaModelo, 3);

        String textoCantidad = JOptionPane.showInputDialog(
                this,
                "Vender \"" + nombre + "\" (stock disponible: " + stockDisponible + ")\nCantidad a vender:",
                "1"
        );
        if (textoCantidad == null) return;
        textoCantidad = textoCantidad.trim();

        int cantidad;
        try {
            cantidad = Integer.parseInt(textoCantidad);
        } catch (NumberFormatException ex) {
            mostrarError("La cantidad debe ser un numero entero");
            return;
        }
        if (cantidad <= 0) {
            mostrarError("La cantidad debe ser mayor a cero");
            return;
        }
        if (cantidad > stockDisponible) {
            mostrarError("No hay suficiente stock. Disponible: " + stockDisponible);
            return;
        }

        try {
            Mensaje respuesta = conexion.enviarYRecibir(
                    new Mensaje(Mensaje.Tipo.VENDER_PRODUCTO, new SolicitudVenta(productoId, cantidad)));

            if (respuesta.getTipo() == Mensaje.Tipo.RESPUESTA_FACTURA) {
                FacturaGenerada factura = (FacturaGenerada) respuesta.getDato();
                Venta venta = factura.getVenta();

                listarProductos();

                JOptionPane.showMessageDialog(
                        this,
                        "Venta registrada.\nTotal: L. " + String.format("%.2f", venta.getTotal()),
                        "Venta exitosa",
                        JOptionPane.INFORMATION_MESSAGE
                );

                abrirOGuardarPdf(factura.getPdf(), "factura_" + venta.getId() + "_");
            } else {
                mostrarError(respuesta.getTexto());
            }
        } catch (Exception ex) {
            mostrarError("Error al registrar la venta: " + ex.getMessage());
        }
    }

    private void verHistorialVentas() {
        try {
            Mensaje respuesta = conexion.enviarYRecibir(new Mensaje(Mensaje.Tipo.LISTAR_VENTAS));

            if (respuesta.getTipo() == Mensaje.Tipo.RESPUESTA_VENTAS) {
                @SuppressWarnings("unchecked")
                List<Venta> ventas = (List<Venta>) respuesta.getDato();
                mostrarDialogoHistorial(ventas);
            } else {
                mostrarError(respuesta.getTexto());
            }
        } catch (Exception ex) {
            mostrarError("Error al obtener el historial: " + ex.getMessage());
        }
    }

    private void mostrarDialogoHistorial(List<Venta> ventas) {
        DefaultTableModel modelo = new DefaultTableModel(
                new Object[]{"ID", "Producto", "Cantidad", "Precio unit.", "Total", "Fecha"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        for (Venta v : ventas) {
            modelo.addRow(new Object[]{
                    v.getId(), v.getProductoNombre(), v.getCantidad(),
                    v.getPrecioUnitario(), v.getTotal(), v.getFecha()
            });
        }

        JTable tabla = new JTable(modelo);
        tabla.setFont(FUENTE_BASE);
        tabla.setRowHeight(24);
        tabla.getTableHeader().setFont(FUENTE_TITULO);

        JScrollPane scroll = new JScrollPane(tabla);
        scroll.setPreferredSize(new Dimension(600, 320));

        JLabel resumen = new JLabel(ventas.size() + " venta" + (ventas.size() == 1 ? "" : "s") + " registrada"
                + (ventas.size() == 1 ? "" : "s"));
        resumen.setFont(FUENTE_BASE);
        resumen.setForeground(COLOR_GRIS_TEXTO);
        resumen.setBorder(new EmptyBorder(6, 4, 0, 0));

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(scroll, BorderLayout.CENTER);
        panel.add(resumen, BorderLayout.SOUTH);

        JOptionPane.showMessageDialog(this, panel, "Historial de ventas", JOptionPane.PLAIN_MESSAGE);
    }

    
    private void abrirOGuardarPdf(byte[] pdf, String prefijoNombre) {
        if (pdf == null || pdf.length == 0) {
            mostrarError("El servidor no devolvio el PDF (llego vacio)");
            return;
        }

        File archivo;
        try {
            archivo = File.createTempFile(prefijoNombre, ".pdf");
            try (FileOutputStream fos = new FileOutputStream(archivo)) {
                fos.write(pdf);
            }
        } catch (Exception ex) {
            mostrarError("No se pudo guardar el PDF: " + ex.getMessage());
            return;
        }

        boolean abierto = false;
        if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.OPEN)) {
            try {
                Desktop.getDesktop().open(archivo);
                abierto = true;
            } catch (Exception ignorado) {
                abierto = false;
            }
        }

        if (!abierto) {
            JOptionPane.showMessageDialog(this, "PDF guardado en:\n" + archivo.getAbsolutePath());
        }
    }

    private void mostrarError(String texto) {
        JOptionPane.showMessageDialog(this, texto, "Error", JOptionPane.ERROR_MESSAGE);
    }

   
    private static class TableRowSorterCompat {
        private final javax.swing.table.TableRowSorter<DefaultTableModel> sorter;

        TableRowSorterCompat(DefaultTableModel modelo) {
            this.sorter = new javax.swing.table.TableRowSorter<>(modelo);
        }

        javax.swing.table.TableRowSorter<DefaultTableModel> getSorter() {
            return sorter;
        }

        void aplicar(String texto) {
            if (texto == null || texto.isBlank()) {
                sorter.setRowFilter(null);
            } else {
                sorter.setRowFilter(javax.swing.RowFilter.regexFilter("(?i)" + java.util.regex.Pattern.quote(texto), 1));
            }
        }
    }
}
