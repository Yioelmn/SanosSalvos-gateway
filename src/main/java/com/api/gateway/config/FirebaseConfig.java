package com.api.gateway.config;

import java.io.IOException;
import java.io.InputStream;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import jakarta.annotation.PostConstruct;

@Configuration
public class FirebaseConfig {

    @PostConstruct
    public void initialize(){
        try{
            // lee el archivo de crendecial de resources
            InputStream serviceAccount = new ClassPathResource("firebase-keys.json").getInputStream();

            FirebaseOptions options = FirebaseOptions.builder()
                .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                .build();

            if(FirebaseApp.getApps().isEmpty()){
                FirebaseApp.initializeApp(options);
                System.out.println("FIREBASE ADMIN INICIALIZADO DESDE API GATEWAY");
            }
        }catch(IOException e){
            System.err.println("ERROR AL INICIALIZAR FIREBASE DESDE GATEWAY");
        }
    }
    
}
