package com.gastontechnologies.usermanagement.database;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
public class DatabaseOptions {

    private String jdbcUrl;
    private String jdbcUsername;
    private String jdbcPassword;
    private String driverClassName;

}
