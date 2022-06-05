package com.gastontechnologies.usermanagement.aws;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.ssm.SsmClient;

@Configuration
public class AwsServiceConfig {

    @Bean
    public SsmClient ssmClient() {
        return SsmClient.builder()
                .region(Region.of(System.getenv("AWS_REGION")))
                .build();
    }

}
