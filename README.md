## How to use

In order to test solution:

1. Update `LIST_OF_SERVERS` in `docker-compose.yml` if needed
2. Run everything by `docker-compose up -d`
3. Query API Gateway with `docker exec -it <gateway_container_id> curl -X GET localhost:9669/get-fortune`.
   See "Faced issues", can't trigger APIs from outside Docker network on Windows.

## Testing

 - *No unit tests* - zero-code approach, everything is configured with Spring Boot automatically.
 Instead of mocking Spring internals, and making tests brittle and complex, I use integration tests.
 Moreover Zuul, Ribbon and Hystrix projects are covered with unit tests (see github).
 - *Integration tests* - start up API Gateway container, mock all service dependencies (fortune)
 services with `WireMock`. Simulate different edge-cases: delays, failures, etc.
 - *End to end tests* - coverage could be achieved with integration tests, running all Docker services 
 for test purposes would be an overkill. End-to-end performance could be tested manually. 

## Solution Details

I use Spring Boot and Netflix OSS stack to implement API gateway with load balancing functionality.
Regret my choice :) because Spring Boot + Netflix OSS has poor and sometimes obsolete documentation. 
Netflix OSS could be replaced with Spring Cloud analogies (i.e. Spring Cloud Gateway).

### API Gateway

Role of API Gateway is played by [Zuul](https://github.com/Netflix/zuul). 
Unlike Netty-based async Zuul2, Zuul 1 use blocking communication backed by Apache HTTP Client. 
Spring Cloud doesn't support Zuul 2, for non-blocking behaviour use Spring Cloud Gateway.

Spring Boot automatically include Zuul embedded proxy after adding dependency to `spring-cloud-starter-netflix-zuul` in
your `pom.xml` and annotating main class with `@EnableZuulProxy`. 
Or by using [Spring Initializr](https://start.spring.io/) with Zuul option choosed.

All Zuul configs could be found at `application.properties` file.

### Load Balancing

Role of layer 7 client-side load balancer is played by [Ribbon](https://github.com/Netflix/ribbon). Ribbon is used by Zuul under the hood. 

All Ribbon-related configurations could be found at `application.properties` file.

### Service registry & scaling services

Ribbon could be easily integrated with service registries like [Eureka](https://github.com/Netflix/eureka), 
but Eureka requires clients (get-fortune services) to register themselves on start-up by calling Eureka server API. 

I treat get-fortune services as third-party that can't be changed. That means I have to hard code list of services 
on application startup, see `application.properties` property called `ribbon.listOfServers`.

I don't want to re-build the Docker image each time list of servers is changed, so I pass the list as environment
variable from `docker-compose`. You can add new get-fortune services by re-starting gateway with new value of `LIST_OF_SERVERS`
variable.

I haven't used Spring Cloud Config or Spring Netflix Archanius since it would be an over engineering for this task.

### Circuit Breaker

Zuul use [Hystryx](https://github.com/Netflix/Hystrix) commands under the hood.

## Faced issues

1. Windows 10 v1607 b14393.3326 doesn't support Docker Desktop, instead you have to run 
Docker Tools, and Docker containers within Oracle VM VirtualBox. [See GitHub ticket](https://github.com/docker/for-win/issues/1263)
2. Docker on older versions of Windows 10 doesn't map ports to localhost. [See GitHub ticket](https://github.com/docker/for-win/issues/204)
3. Zuul/Ribbon/Hystrix documentation is obsolete, it lacks one very significant property - 
retry interval (exponential or at least fixed). This cause Zuul to fire retries immediately, and 
with latencies up to 5 seconds it makes situation even worse. Take a look at `ApiGatewayHttpServerBasedIntegrationTest`