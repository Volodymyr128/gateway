## How to use

## Solution Details

I use Spring Boot and Netflix OSS stack to implement API gateway with load balancing functionality.

### API Gateway

Role of API Gateway is played by [Zuul](https://github.com/Netflix/zuul). By default Zuul is 
backed by Apache HTTP Client.

Spring Boot automatically include Zuul embedded proxy after adding dependency to `spring-cloud-starter-netflix-zuul` in
your `pom.xml` and annotating main class with `@EnableZuulProxy`. 
Or by using [Spring Initializr](https://start.spring.io/) with Zuul option choosed.

All Zuul configs could be found at `application.properties` file.

### Load Balancing

Role of layer 7 load balancer is played by [Ribbon](https://github.com/Netflix/ribbon). Ribbon is used by Zuul under the hood. 

All Ribbon-related configurations could be found at `application.properties` file.

### Service registry & scaling services

Ribbon could be easily integrated with service registries like [Eureka](https://github.com/Netflix/eureka), 
but Eureka requires clients (get-fortune services) to register themselves on start-up by calling Eureka server API. 

I treat get-fortune services as third-party that can't be changed. That means I have to hard code list of services 
on application startup, see `application.properties` property called `ribbon.listOfServers`.

I don't want to re-build the Docker image each time list of servers is changed, so I pass the list as environment
variable from `docker-compose`. You can add new get-fortune services by re-starting gateway with new value of `LIST_OF_SERVERS`
variable.

### Circuit Breaker

Zuul use [Hystryx](https://github.com/Netflix/Hystrix) commands under the hood.

### Monitoring 

Spring Boot Actuator endpoints are enabled in DEV profile, so any failures that might occur when routing requests
can be viewed at `actuator/metrics` endpoint and will the name `ZUUL::EXCEPTION:errorCause:statusCode`

## Faced issues

1.  