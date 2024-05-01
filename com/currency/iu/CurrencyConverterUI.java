package com.currency.iu;

import com.currency.api.CurrencyExchangeAPI;
import com.google.gson.JsonObject;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.Set;

public class CurrencyConverterUI extends JFrame {
    private CurrencyExchangeAPI exchangeAPI = new CurrencyExchangeAPI();
    private JComboBox<String> sourceCurrencyBox;
    private JComboBox<String> targetCurrencyBox;
    private JTextField amountField;
    private JTextArea resultArea;
    private JLabel resultLabel;

    public CurrencyConverterUI() {
        super("Currency Converter");
        createUI();
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(500, 350);
        setVisible(true);
        initializeCurrencyBoxes();
    }

    private void createUI() {
        setLayout(new BorderLayout());

        JPanel northPanel = new JPanel();
        sourceCurrencyBox = new JComboBox<>();
        targetCurrencyBox = new JComboBox<>();
        amountField = new JTextField(10);
        JButton convertButton = new JButton("Convert");
        convertButton.addActionListener(this::convertCurrency);

        northPanel.add(new JLabel("Base Currency:"));
        northPanel.add(sourceCurrencyBox);
        northPanel.add(new JLabel("Target Currency:"));
        northPanel.add(targetCurrencyBox);
        northPanel.add(new JLabel("Amount:"));
        northPanel.add(amountField);
        northPanel.add(convertButton);

        add(northPanel, BorderLayout.NORTH);

        resultLabel = new JLabel(" ", SwingConstants.CENTER);
        resultLabel.setFont(new Font("Serif", Font.BOLD, 24));
        add(resultLabel, BorderLayout.CENTER);

        resultArea = new JTextArea(5, 30);
        resultArea.setEditable(false);
        add(new JScrollPane(resultArea), BorderLayout.SOUTH);
    }

    private void initializeCurrencyBoxes() {
        try {
            Set<String> currencies = exchangeAPI.getAvailableCurrencies();
            DefaultComboBoxModel<String> model = new DefaultComboBoxModel<>(currencies.toArray(new String[0]));
            sourceCurrencyBox.setModel(model);
            targetCurrencyBox.setModel(new DefaultComboBoxModel<>(currencies.toArray(new String[0])));
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Failed to load currency data: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void convertCurrency(ActionEvent e) {
        String sourceCurrency = (String) sourceCurrencyBox.getSelectedItem();
        String targetCurrency = (String) targetCurrencyBox.getSelectedItem();
        String amountText = amountField.getText();
        try {
            double amount = Double.parseDouble(amountText);
            double convertedAmount = exchangeAPI.convertCurrency(sourceCurrency, targetCurrency, amount);
            resultLabel.setText(String.format("%,.2f %s", convertedAmount, targetCurrency));
            resultArea.setText(String.format("Converted %s %s to %,.2f %s", amountText, sourceCurrency, convertedAmount, targetCurrency));
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Please enter a valid number for amount.", "Input Error", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            resultArea.setText("Error: " + ex.getMessage());
            JOptionPane.showMessageDialog(this, "Error retrieving or converting currency: " + ex.getMessage(), "Conversion Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
