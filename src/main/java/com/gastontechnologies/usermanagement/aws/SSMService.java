package com.gastontechnologies.usermanagement.aws;

import com.gastontechnologies.usermanagement.UserManagementApplication;
import com.gastontechnologies.usermanagement.database.DatabaseOptions;
import com.gastontechnologies.usermanagement.function.ParameterStoreKeyInput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.ssm.SsmClient;
import software.amazon.awssdk.services.ssm.model.GetParameterRequest;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SSMService {

    private static final Logger log = LoggerFactory.getLogger(SSMService.class);

    @Autowired
    private SsmClient ssmClient;

    public List<DatabaseOptions> buildDbOptions(ParameterStoreKeyInput parameterStoreKeyInput) {
        GetParameterRequest tenantsRequest = GetParameterRequest.builder()
                .name(parameterStoreKeyInput.getTenantsSSMKey())
                .withDecryption(false)
                .build();

        String[] tenants = ssmClient.getParameter(tenantsRequest).parameter().value().split(",");

        log.info("tenants: {}", Arrays.toString(tenants));

        return Arrays.stream(tenants).map(tenant -> {
            String jdbcUrl = getParameterStoreParam(tenant + parameterStoreKeyInput.getTenantSSMKeyDelimiter() + parameterStoreKeyInput.getDbUrlSSMKey(), true);
            String jdbcUsername = getParameterStoreParam(tenant + parameterStoreKeyInput.getTenantSSMKeyDelimiter() + parameterStoreKeyInput.getDbUsernameSSMKey(), true);
            String jdbcPassword = getParameterStoreParam(tenant + parameterStoreKeyInput.getTenantSSMKeyDelimiter() + parameterStoreKeyInput.getDbPasswordSSMKey(), true);
            String driverClassName = getParameterStoreParam(tenant + parameterStoreKeyInput.getTenantSSMKeyDelimiter() + parameterStoreKeyInput.getDbDriverClassNameSSMKey(), false);
            return DatabaseOptions.builder().jdbcPassword(jdbcPassword).jdbcUrl(jdbcUrl).jdbcUsername(jdbcUsername).driverClassName(driverClassName).build();
        }).collect(Collectors.toList());
    }

    public String getParameterStoreParam(String key, boolean withDecryption) {
        return ssmClient.getParameter(GetParameterRequest.builder()
                .name(key)
                .withDecryption(withDecryption)
                .build()).parameter().value();
    }

}
