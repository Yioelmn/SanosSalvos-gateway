package com.api.gateway.config;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import jakarta.annotation.PostConstruct;

@Configuration
public class FirebaseConfig {

    @Value("${FIREBASE_CONFIG_PATH:local}")
    private String configPath;

    @PostConstruct
    public void initialize() {
        try {
            InputStream serviceAccount;

            if ("local".equals(configPath)) {
                serviceAccount = new ClassPathResource("firebase-keys.json").getInputStream();
                System.out.println("FIREBASE: Inicializando en modo LOCAL (Resources)");
            } else {
                serviceAccount = new FileInputStream(configPath);
                System.out.println("FIREBASE: Inicializando en modo PRODUCCIÓN desde: " + configPath);
            }

            FirebaseOptions options = FirebaseOptions.builder()
                .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                .build();

            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(options);
                System.out.println("FIREBASE ADMIN INICIALIZADO CON ÉXITO DESDE API GATEWAY");
            }
        } catch (IOException e) {
            System.err.println("ERROR CRÍTICO AL INICIALIZAR FIREBASE DESDE GATEWAY: " + e.getMessage());
        }
    }
}