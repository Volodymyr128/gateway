package com.vbakh.gateway;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import lombok.RequiredArgsConstructor;
import org.junit.*;
import org.springframework.http.ResponseEntity;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import static org.junit.Assert.assertEquals;

public class ApiGatewayHttpServerBasedIntegrationTest extends ApiGatewayTestBase {

    private static final SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss.SSS");

    @Test
    public void test_multiple_concurrent_call_with_different_fixed_timeout() throws IOException {
        startEmbeddedHttpServer(9001, 1_000);
        startEmbeddedHttpServer(9002, 100);
        List<ResponseEntity<String>> responses = simulateAsyncClientRequests(10);

        assertEquals(1, responses.stream().filter(r -> r.getBody().equals(getResponse(9001))).count());
        assertEquals(9, responses.stream().filter(r -> r.getBody().equals(getResponse(9002))).count());
    }

    /**
     * Unlike WireMock or MockServer, HttpServer is able to return "Worker is busy" on concurrent requests with help
     * of custom handling logic at {@link NoConcurrencyHandler}
     * @param port
     * @param delay
     * @throws IOException
     */
    private void startEmbeddedHttpServer(Integer port, Integer delay) throws IOException {
        HttpServer server = HttpServer.create();
        server.bind(new InetSocketAddress(port), port);
        server.createContext(SERVICE_PATH, new NoConcurrencyHandler(port, delay));
        server.setExecutor(Executors.newFixedThreadPool(20));
        server.start();
    }

    @RequiredArgsConstructor
    static class NoConcurrencyHandler implements HttpHandler {

        private final AtomicBoolean isBusy = new AtomicBoolean(false);
        private final Integer port;
        private final Integer delay;

        @Override
        public void handle(HttpExchange httpExchange) throws IOException {
            OutputStream os = httpExchange.getResponseBody();
            try {
                if (isBusy.getAndSet(true)) {
                    String time = sdf.format(new Date(System.currentTimeMillis()));
                    System.out.println(String.format("%s service %d is busy", time, port));
                    httpExchange.sendResponseHeaders(503, 0);
                    os.write("The worker is busy".getBytes());
                } else {
                    String time = sdf.format(new Date(System.currentTimeMillis()));
                    System.out.println(String.format("%s service %d is invoked", time, port));
                    Thread.sleep(delay);
                    httpExchange.sendResponseHeaders(200, 0);
                    os.write(getResponse(port).getBytes());
                    isBusy.set(false);
                }
            } catch (InterruptedException e) {
                isBusy.set(false);
                throw new RuntimeException(e);
            } finally {
                os.close();
            }
        }
    }
}
