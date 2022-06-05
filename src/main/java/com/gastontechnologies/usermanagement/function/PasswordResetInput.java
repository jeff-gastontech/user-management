package com.gastontechnologies.usermanagement.function;

import lombok.*;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
public class PasswordResetInput {
    private List<String> schemas;
    private String username;
}
