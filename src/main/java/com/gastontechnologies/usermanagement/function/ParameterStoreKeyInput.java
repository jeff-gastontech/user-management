package com.gastontechnologies.usermanagement.function;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
public class ParameterStoreKeyInput {

    private String dbUsernameSSMKey;
    private String dbPasswordSSMKey;
    private String dbUrlSSMKey;
    private String dbDriverClassNameSSMKey;
    private String tenantsSSMKey;
    // Something in the form of "." in "tenant1.db.url". It's what splits tenant1 and db.
    private String tenantSSMKeyDelimiter;

}
