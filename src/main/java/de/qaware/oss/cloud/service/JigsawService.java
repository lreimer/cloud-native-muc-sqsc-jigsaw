package de.qaware.oss.cloud.service;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.util.Optional;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

import static java.nio.charset.Charset.forName;

/**
 * A simple {@link com.sun.net.httpserver.HttpServer} that offers a
 * small REST service to respond stuff.
 */
public class JigsawService {

    private static final Logger LOGGER = Logger.getLogger(JigsawService.class.getName());

    private static final InetSocketAddress ADDRESS = new InetSocketAddress("0.0.0.0", 9000);

    private static final String CONTENT_TYPE = "Content-Type";
    private static final String TEXT_PLAIN = "application/json; charset=utf-8";

    private final Executor executor;

    private JigsawService() {
        this.executor = Executors.newFixedThreadPool(4);
    }

    public static void main(String[] args) {
        // when this is done, start the mail service
        JigsawService service = new JigsawService();
        service.start();
    }

    private void start() {
        final HttpServer httpServer;
        try {
            httpServer = HttpServer.create(ADDRESS, 8);
            // httpServer.setExecutor(executor);
        } catch (final IOException e) {
            LOGGER.severe("Failed to create HTTP server: " + e.getMessage());
            throw new IllegalStateException(e);
        }

        httpServer.createContext("/", this::handleContext);

        LOGGER.info("Starting HTTP server");
        httpServer.start();
        Runtime.getRuntime().addShutdownHook(new Thread(() -> httpServer.stop(0)));
    }

    private void handleContext(HttpExchange exchange) {
        switch (exchange.getRequestURI().toString()) {
            case "/":
                handleRootContext(exchange);
                break;
            default:
                handleNotFound(exchange);
                break;
        }
    }

    private void handleRootContext(HttpExchange exchange) {
        LOGGER.info("Handling '/' request context.");
        consumeInputStream(exchange.getRequestBody());

        Optional<String> message = Optional.ofNullable(System.getenv("MESSAGE"));
        String value = message.orElse("Hello JigSaw Service Cloud Native Night with SquareScale!");

        String payload = "{ \"message\": \"" + value + "\" }";
        byte[] bytes = payload.getBytes(forName("UTF-8"));

        try {
            exchange.getResponseHeaders().add(CONTENT_TYPE, TEXT_PLAIN);
            exchange.sendResponseHeaders(200, bytes.length);
            exchange.getResponseBody().write(bytes);
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Failed to send response.", e);
        } finally {
            exchange.close();
        }
    }

    private void handleNotFound(HttpExchange exchange) {
        LOGGER.log(Level.WARNING, "Handling unknown {0} request context.", exchange.getRequestURI());
        consumeInputStream(exchange.getRequestBody());

        try {
            exchange.sendResponseHeaders(404, -1);
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Failed to send response.", e);
        } finally {
            exchange.close();
        }
    }

    private void consumeInputStream(final InputStream is) {
        if (is == null)
            return;
        try {
            while (is.read() != -1) { /* null loop */ }
        } catch (final IOException e) {
            LOGGER.warning("Failed to read request body: " + e.getMessage());
        }
    }
}
