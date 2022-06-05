package com.gastontechnologies.usermanagement.graph;

import com.azure.identity.ClientSecretCredential;
import com.azure.identity.ClientSecretCredentialBuilder;
import com.gastontechnologies.usermanagement.aws.SSMService;
import com.microsoft.graph.authentication.TokenCredentialAuthProvider;
import com.microsoft.graph.requests.GraphServiceClient;
import okhttp3.Request;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class GraphConfig {

    @Autowired
    private SSMService ssmService;

    @Bean
    public ClientSecretCredential clientSecretCredential() {
        return new ClientSecretCredentialBuilder()
                .clientId(ssmService.getParameterStoreParam("azure.ad.app.id", true))
                .clientSecret(ssmService.getParameterStoreParam("azure.ad.app.secret", true))
                .tenantId(ssmService.getParameterStoreParam("azure.ad.app.tenant", true))
                .build();
    }

    @Bean
    public TokenCredentialAuthProvider authProvider(ClientSecretCredential clientSecretCredential) {
        return new TokenCredentialAuthProvider(List.of(ssmService.getParameterStoreParam("azure.ad.app.scopes", false)), clientSecretCredential);
    }

    @Bean
    public GraphServiceClient<Request> graphClient(TokenCredentialAuthProvider authProvider) {
        return GraphServiceClient
                .builder()
                .authenticationProvider(authProvider)
                .buildClient();
    }

}
