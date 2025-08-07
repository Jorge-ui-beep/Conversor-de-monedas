import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

public class Main {
    private static final String API_KEY = "b626126799e19017ca390706";

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        boolean continuar = true;

        System.out.println("=== Sea bienvenido al Conversor de Monedas ===");

        while (continuar) {
            System.out.println("\nSeleccione una opción:");
            System.out.println("1. Dólar >> Peso Argentino");
            System.out.println("2. Peso Argentino >> Dólar");
            System.out.println("3. Dólar >> Real Brasileño");
            System.out.println("4. Real Brasileño >> Dólar");
            System.out.println("5. Dólar >> Peso Colombiano");
            System.out.println("6. Peso Colombiano >> Dólar");
            System.out.println("7. Salir");
            System.out.print("Opción: ");
            int opcion = scanner.nextInt();

            if (opcion == 7) {
                System.out.println("Gracias por usar el conversor.");
                continuar = false;
                break;
            }

            System.out.print("Ingrese la cantidad a convertir: ");
            double cantidad = scanner.nextDouble();

            String from = "";
            String to = "";

            switch (opcion) {
                case 1:
                    from = "USD";
                    to = "ARS";
                    break;
                case 2:
                    from = "ARS";
                    to = "USD";
                    break;
                case 3:
                    from = "USD";
                    to = "BRL";
                    break;
                case 4:
                    from = "BRL";
                    to = "USD";
                    break;
                case 5:
                    from = "USD";
                    to = "COP";
                    break;
                case 6:
                    from = "COP";
                    to = "USD";
                    break;
                default:
                    System.out.println("Opción inválida.");
                    continue;
            }

            try {
                double tasa = obtenerTasaCambio(from, to);
                double resultado = cantidad * tasa;
                System.out.printf("Resultado: %.2f %s%n", resultado, to);
            } catch (Exception e) {
                System.out.println("Error al obtener la tasa de cambio: " + e.getMessage());
            }
        }

        scanner.close();
    }

    public static double obtenerTasaCambio(String from, String to) throws Exception {
        String urlStr;

        if (from.equals("USD")) {
            // Cuando la base es USD, usamos el endpoint latest/USD para obtener todas las tasas y buscamos la moneda destino
            urlStr = String.format("https://v6.exchangerate-api.com/v6/%s/latest/%s", API_KEY, from);
            URL url = new URL(urlStr);
            HttpURLConnection conexion = (HttpURLConnection) url.openConnection();
            conexion.setRequestMethod("GET");

            BufferedReader reader = new BufferedReader(new InputStreamReader(conexion.getInputStream()));
            StringBuilder response = new StringBuilder();
            String linea;

            while ((linea = reader.readLine()) != null) {
                response.append(linea);
            }
            reader.close();

            String json = response.toString();
            // Busca el valor de la tasa de la moneda destino dentro del JSON sin librerías externas
            String buscar = "\"" + to + "\":";
            int index = json.indexOf(buscar);
            if (index == -1) {
                throw new Exception("No se encontró la tasa de cambio para " + to);
            }
            int start = index + buscar.length();
            int end = json.indexOf(",", start);
            if (end == -1) end = json.indexOf("}", start);
            String tasaStr = json.substring(start, end).trim();
            return Double.parseDouble(tasaStr);

        } else {
            // Si la base NO es USD, usamos el endpoint pair para la conversión directa
            urlStr = String.format("https://v6.exchangerate-api.com/v6/%s/pair/%s/%s", API_KEY, from, to);
            URL url = new URL(urlStr);
            HttpURLConnection conexion = (HttpURLConnection) url.openConnection();
            conexion.setRequestMethod("GET");

            BufferedReader reader = new BufferedReader(new InputStreamReader(conexion.getInputStream()));
            StringBuilder response = new StringBuilder();
            String linea;

            while ((linea = reader.readLine()) != null) {
                response.append(linea);
            }
            reader.close();

            String json = response.toString();
            String buscar = "\"conversion_rate\":";
            int index = json.indexOf(buscar);
            if (index == -1) {
                throw new Exception("No se encontró la tasa de cambio para " + from + " a " + to);
            }
            int start = index + buscar.length();
            int end = json.indexOf(",", start);
            if (end == -1) end = json.indexOf("}", start);
            String tasaStr = json.substring(start, end).trim();
            return Double.parseDouble(tasaStr);
        }
    }
}
