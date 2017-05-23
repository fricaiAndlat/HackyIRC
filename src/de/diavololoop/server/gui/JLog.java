package de.diavololoop.server.gui;

import javax.swing.*;
import java.awt.*;
import java.util.function.BiConsumer;

/**
 * Created by gast1 on 22.05.17.
 */
public class JLog extends JPanel {

    private JEditorPane logPane;
    private JTextField field;
    private JScrollPane scroll;

    private String name;

    private BiConsumer<String, String> messageCallback = (key, msg) -> {};

    public JLog(String name){

        this.name = name;

        logPane = new JEditorPane();
        logPane.setEditable(false);

        scroll = new JScrollPane(logPane);

        field = new JTextField();

        setLayout(new BorderLayout());

        add(new JLabel(name), BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);
        add(field, BorderLayout.SOUTH);

        field.addActionListener(event -> {
            messageCallback.accept(name, field.getText());
            field.setText("");
        });


    }

    public void log(String msg) {

        logPane.setText(logPane.getText() + "\r\n" + msg);

    }

    public void setMessageCallback(BiConsumer<String, String> messageCallback){
        this.messageCallback = messageCallback;
    }
}
