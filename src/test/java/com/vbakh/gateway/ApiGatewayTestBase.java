package com.vbakh.gateway;

import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.IntStream;
import static java.util.stream.Collectors.toList;

@ActiveProfiles("test")
@TestPropertySource(locations = "classpath:/application-test.yml")
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public class ApiGatewayTestBase {

    public static final String GATEWAY_URL = "http://localhost:9669/get-fortune";
    public static final String SERVICE_PATH = "/get-fortune";
    private final TestRestTemplate restTemplate = new TestRestTemplate();
    private ExecutorService executor;

    @Before
    public void setUp() {
        executor = Executors.newFixedThreadPool(10);
    }

    @After
    public void tearDown() {
        executor.shutdown();
    }

    /**
     * Clients requests are async while Zuul->Service requests are blocking
     * @return
     */
    protected List<ResponseEntity<String>> simulateAsyncClientRequests(int nRequests) {
        return IntStream
                .range(0, nRequests)
                .boxed()
                .map(i -> executor.submit(() -> restTemplate.getForEntity(GATEWAY_URL, String.class)))
                .collect(toList())
                .stream()
                .map(this::resolve)
                .collect(toList());
    }

    private <T> T resolve(Future<T> future) {
        try {
            return future.get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    static String getResponse(Integer port) {
        return "Response from " + port;
    }
}
