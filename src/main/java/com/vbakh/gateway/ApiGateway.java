package com.vbakh.gateway;

import com.netflix.client.config.DefaultClientConfigImpl;
import com.netflix.loadbalancer.*;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.cloud.client.loadbalancer.LoadBalancedRetryFactory;
import org.springframework.cloud.netflix.ribbon.ZonePreferenceServerListFilter;
import org.springframework.cloud.netflix.zuul.EnableZuulProxy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.retry.backoff.BackOffPolicy;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.web.client.RestTemplate;

import static com.netflix.client.config.CommonClientConfigKey.ListOfServers;

@SpringBootApplication
@EnableZuulProxy
@Configuration
//@EnableRetry
public class ApiGateway {

    public static void main(String[] args) {
        SpringApplication.run(ApiGateway.class, args);
    }

    @Bean
    public RetryRule retryRule() {
        RetryRule rule = new RetryRule();
        rule.setMaxRetryMillis(2_000);
        return rule;
    }

//    @Bean
//    public ILoadBalancer loadBalancer() {
//        RetryRule rule = new RetryRule();
//        rule.setMaxRetryMillis(2_000);
//        DefaultClientConfigImpl clientConfig = new DefaultClientConfigImpl();
//        clientConfig.setProperty(ListOfServers, "localhost:9001,list:9002");
//        ConfigurationBasedServerList serverList = new ConfigurationBasedServerList();
//        serverList.initWithNiwsConfig(clientConfig);
//        DynamicServerListLoadBalancer lb = new DynamicServerListLoadBalancer();
//        lb.setRule(rule);
//        lb.setServerListImpl(serverList);
//        return lb;
//    }

//    @LoadBalanced
//    @Bean
//    RestTemplate restTemplate() {
//        return new RestTemplate();
//    }
//
//    @Bean
//    LoadBalancedRetryFactory retryFactory() {
//        return new LoadBalancedRetryFactory() {
//            @Override
//            public BackOffPolicy createBackOffPolicy(String service) {
//                ExponentialBackOffPolicy policy = new ExponentialBackOffPolicy();
//                policy.setInitialInterval(1_000);
//                policy.setMultiplier(2.);
//                policy.setMaxInterval(10_000);
//                return policy;
//            }
//        };
//    }
}