package com.gastontechnologies.usermanagement.user;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class User {

    @EqualsAndHashCode.Include
    private String mysqlUsername;
    private String mysqlPassword;
    private String jdbcUrl;
    @EqualsAndHashCode.Include
    private String schema;

}
