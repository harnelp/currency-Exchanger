package com.currency.api;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Set;
import java.util.TreeSet;

public class CurrencyExchangeAPI {
    private static final String API_KEY = "0b24e788dc35189080ca0b1b";
    private static final String BASE_URL = "https://v6.exchangerate-api.com/v6/";
    private static final String FILE_PATH = "rates.json";

    public JsonObject getExchangeRates(String baseCurrency) throws IOException {
        String urlStr = BASE_URL + API_KEY + "/latest/" + baseCurrency;
        HttpURLConnection request = (HttpURLConnection) new URL(urlStr).openConnection();
        request.setRequestMethod("GET");
        request.setConnectTimeout(10000);
        request.setReadTimeout(10000);

        int status = request.getResponseCode();
        if (status != HttpURLConnection.HTTP_OK) {
            throw new IOException("Error: HTTP response code: " + status);
        }

        try (InputStreamReader reader = new InputStreamReader(request.getInputStream())) {
            JsonObject jsonObject = JsonParser.parseReader(reader).getAsJsonObject();
            saveRatesToFile(jsonObject);
            return jsonObject;
        } finally {
            request.disconnect();
        }
    }

    private void saveRatesToFile(JsonObject rates) {
        try (FileWriter writer = new FileWriter(FILE_PATH)) {
            writer.write(rates.toString());
        } catch (IOException e) {
            System.err.println("Error saving rates to file: " + e.getMessage());
        }
    }

    public JsonObject readRatesFromFile() throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(FILE_PATH))) {
            return JsonParser.parseReader(reader).getAsJsonObject();
        } catch (FileNotFoundException e) {
            System.err.println("Rates file not found, fetching new rates.");
            return getExchangeRates("USD");
        }
    }

    public Set<String> getAvailableCurrencies() throws IOException {
        JsonObject rates = readRatesFromFile();
        JsonObject conversionRates = rates.getAsJsonObject("conversion_rates");
        return new TreeSet<>(conversionRates.keySet());
    }

    public double convertCurrency(String sourceCurrency, String targetCurrency, double amount) throws IOException {
        JsonObject rates = readRatesFromFile();
        if (!rates.has("conversion_rates")) {
            throw new IOException("Conversion rates not found in the file.");
        }
        JsonObject conversionRates = rates.getAsJsonObject("conversion_rates");

        if (!conversionRates.has(sourceCurrency) || !conversionRates.has(targetCurrency)) {
            throw new IOException("One of the currency rates not found.");
        }
        double sourceRate = conversionRates.get(sourceCurrency).getAsDouble();
        double targetRate = conversionRates.get(targetCurrency).getAsDouble();

        double baseAmount = amount / sourceRate; // Convertir monto a la moneda base (USD)
        return baseAmount * targetRate; // Convertir de la moneda base a la moneda de destino
    }
}
