package com.gastontechnologies.usermanagement.database;

import lombok.*;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
public class DatabaseGroupMapping {

    private List<String> schemas;
    private List<String> permissions;
    private String groupName;

}
