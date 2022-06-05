package com.gastontechnologies.usermanagement.database;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
public class SqlOutput {

    private String errorMessage;
    private boolean createdSuccessfully;
    private boolean deletedSuccessfully;
    private boolean resetSuccessfully;
    private String schema;
    private String username;
    private String password;
    private String jdbcUrl;

}
