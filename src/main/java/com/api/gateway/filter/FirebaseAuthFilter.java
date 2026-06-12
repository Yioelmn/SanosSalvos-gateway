package com.api.gateway.filter;

import com.google.firebase.auth.FirebaseAuth;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Component
public class FirebaseAuthFilter extends AbstractGatewayFilterFactory<FirebaseAuthFilter.Config> {

    public FirebaseAuthFilter() {
        super(FirebaseAuthFilter.Config.class);
    }

    public static class Config {
        // Clase interna requerida por Spring Cloud Gateway
    }

    @Override
    public GatewayFilter apply(FirebaseAuthFilter.Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            String path = request.getURI().getPath();

            //Rutas públicas
            if (path.contains("/auth/login") || path.contains("/auth/register") || path.contains("/health")) {
                return chain.filter(exchange);
            }

            // Comprobacion de Header Authorization
            if (!request.getHeaders().containsKey("Authorization")) {
                return onError(exchange, "Header Authorization ausente", HttpStatus.UNAUTHORIZED);
            }

            String authHeader = request.getHeaders().getFirst("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return onError(exchange, "Formato de Token invalido (Debe ser Bearer)", HttpStatus.UNAUTHORIZED);
            }

            String token = authHeader.substring(7);

            // Validando el token de Firebase de manera reactiva
            return Mono.fromCallable(() -> FirebaseAuth.getInstance().verifyIdToken(token))
                    .subscribeOn(Schedulers.boundedElastic())
                    .flatMap(decodedToken -> {
                        // Inyectamos el UID y Email para los microservicios
                        ServerHttpRequest modifiedRequest = request.mutate()
                                .header("X-User-Uid", decodedToken.getUid())
                                .header("X-User-Email", decodedToken.getEmail())
                                .build();
                        return chain.filter(exchange.mutate().request(modifiedRequest).build());
                    })
                    .onErrorResume(e -> onError(exchange, "Token de Firebase invalido", HttpStatus.UNAUTHORIZED));
        };
    }

    private Mono<Void> onError(ServerWebExchange exchange, String err, HttpStatus status) {
        System.out.println("Acceso bloqueado: " + err);
        exchange.getResponse().setStatusCode(status);
        return exchange.getResponse().setComplete();
    }
}