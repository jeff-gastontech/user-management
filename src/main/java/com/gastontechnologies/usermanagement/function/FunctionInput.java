package com.gastontechnologies.usermanagement.function;

import com.gastontechnologies.usermanagement.database.DatabaseGroupMapping;
import lombok.*;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
public class FunctionInput {
    private List<DatabaseGroupMapping> groupMappings;
    private List<PasswordResetInput> usersForPasswordResets;
    private ParameterStoreKeyInput parameterStoreKeyInput;
}
