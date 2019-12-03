package com.vbakh.gateway;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import org.junit.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toSet;
import static org.junit.Assert.assertTrue;

public class ApiGatewayWireMockBasedIntegrationTest extends ApiGatewayTestBase {

    @Rule
    public WireMockRule service1 = new WireMockRule(9001);
    @Rule
    public WireMockRule service2 = new WireMockRule(9002);

    @Test
    public void test_request_redirected_among_all_services() throws IOException {
        mockSuccessfulRequest(service1, 9001, 100);
        mockSuccessfulRequest(service2, 9002, 100);
        List<ResponseEntity<String>> responses = simulateAsyncClientRequests(2);
        Set<String> bodies = responses.stream()
                .map(ResponseEntity::getBody)
                .collect(toSet());
        Set<String> expected = new HashSet<>(asList(getResponse(9001), getResponse(9002)));
        assertTrue(bodies.equals(expected));
    }

    @Test
    public void test_request_handled_after_one_service_failure() throws  IOException {
        mockSuccessfulRequest(service1, 9001, 100);
        mockFailureRequest(service2, 100);
        List<ResponseEntity<String>> responses = simulateAsyncClientRequests(2);
        Set<String> bodies = responses.stream()
                .map(ResponseEntity::getBody)
                .collect(toSet());
        //both responses from the first service, since the second service failure should be redirected to the first service
        Set<String> expected = new HashSet<>(asList(getResponse(9001), getResponse(9001)));
        assertTrue(bodies.equals(expected));
    }

    private void mockSuccessfulRequest(WireMockRule rule, Integer port, Integer delay) {
        rule.stubFor(
                get(urlPathEqualTo(SERVICE_PATH))
                        .willReturn(aResponse()
                                .withHeader("Content-Type", MediaType.TEXT_PLAIN_VALUE)
                                .withBody(getResponse(port))
                                .withFixedDelay(delay)
                                .withStatus(HttpStatus.OK.value())));
    }

    private void mockFailureRequest(WireMockRule rule, Integer delay) {
        rule.stubFor(
                get(urlPathEqualTo(SERVICE_PATH))
                        .willReturn(aResponse()
                                .withHeader("Content-Type", MediaType.TEXT_PLAIN_VALUE)
                                .withBody("Worker is busy")
                                .withFixedDelay(delay)
                                .withStatus(HttpStatus.SERVICE_UNAVAILABLE.value())));
    }
}
