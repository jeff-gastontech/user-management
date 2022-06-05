package com.gastontechnologies.usermanagement.function;

import com.gastontechnologies.usermanagement.user.User;
import lombok.*;

import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
public class FunctionOutput {
    private Set<User> successfulOutputs;
    private Set<User> deletedOutputs;
    private Set<User> failedOutputs;
}
