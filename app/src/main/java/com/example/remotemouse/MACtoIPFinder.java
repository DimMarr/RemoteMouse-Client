package com.example.remotemouse;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MACtoIPFinder {

    public static String findIPAddress(String targetMacAddress) {
        String ipAddress = null;
        try {
            // Exécute la commande 'arp -a' pour obtenir les adresses IP et MAC sur le réseau local
            Process process = Runtime.getRuntime().exec("arp -a");

            // Lit la sortie de la commande
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;

            // Crée un modèle de recherche qui remplace les séparateurs ':' ou '-' par une alternative
            String macRegex = targetMacAddress.replace(":", "[:-]").replace("-", "[:-]");
            Pattern macPattern = Pattern.compile(macRegex, Pattern.CASE_INSENSITIVE);

            // Recherche l'adresse MAC dans la sortie
            while ((line = reader.readLine()) != null) {
                if (macPattern.matcher(line).find()) {
                    // Extrait l'adresse IP de la ligne contenant l'adresse MAC
                    Matcher ipMatcher = Pattern.compile("\\d+\\.\\d+\\.\\d+\\.\\d+").matcher(line);
                    if (ipMatcher.find()) {
                        ipAddress = ipMatcher.group();
                        break;
                    }
                }
            }
            reader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ipAddress;
    }

    public static void main(String[] args) {
        String macAddress = "5e-fc-9c-ca-3a-60";  // Remplacez par l'adresse MAC que vous recherchez
        String ipAddress = findIPAddress(macAddress);

        if (ipAddress != null) {
            System.out.println("Adresse IP trouvée pour " + macAddress + ": " + ipAddress);
        } else {
            System.out.println("Aucune adresse IP trouvée pour " + macAddress);
        }
    }
}
