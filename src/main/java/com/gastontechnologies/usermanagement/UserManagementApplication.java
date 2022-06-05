package com.gastontechnologies.usermanagement;

import com.gastontechnologies.usermanagement.aws.SSMService;
import com.gastontechnologies.usermanagement.user.DbUserService;
import com.gastontechnologies.usermanagement.database.DatabaseOptions;
import com.gastontechnologies.usermanagement.function.FunctionInput;
import com.gastontechnologies.usermanagement.function.FunctionOutput;
import com.gastontechnologies.usermanagement.database.SqlOutput;
import com.gastontechnologies.usermanagement.group.GroupService;
import com.gastontechnologies.usermanagement.user.AdUserService;
import com.gastontechnologies.usermanagement.user.User;
import com.microsoft.graph.models.Group;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.context.annotation.Bean;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

@SpringBootApplication(exclude = {
        DataSourceAutoConfiguration.class,
        DataSourceTransactionManagerAutoConfiguration.class,
        HibernateJpaAutoConfiguration.class
})
public class UserManagementApplication {

    private static final Logger log = LoggerFactory.getLogger(UserManagementApplication.class);

    @Autowired
    private GroupService groupService;

    @Autowired
    private AdUserService userService;

    @Autowired
    private DbUserService databaseService;

    @Autowired
    private SSMService ssmService;

    public static void main(String[] args) {
        SpringApplication.run(UserManagementApplication.class, args);
    }

    @Bean
    public Function<FunctionInput, FunctionOutput> syncUsers() {
        log.info("Syncing users");

        return value -> {

            List<DatabaseOptions> dbOptions = ssmService.buildDbOptions(value.getParameterStoreKeyInput());

            Set<User> usersCreated = new HashSet<>();
            Set<User> usersDeleted = new HashSet<>();
            Set<User> failedUsers = new HashSet<>();

            value.getGroupMappings().forEach(groupMapping -> {
                List<Group> groupNames = groupService.getGroupsByName(groupMapping.getGroupName());
                List<User> users = new ArrayList<>();

                groupNames.forEach(
                        group ->
                                users.addAll(
                                        userService.getUsersByGroup(group)
                                )
                );

                log.info(users.toString());

                users.forEach(user -> {
                    List<SqlOutput> outputs = databaseService.syncUser(user, groupMapping, dbOptions);

                    outputs.forEach(output -> {
                        if (output.isCreatedSuccessfully()) {
                            usersCreated.add(User.builder().mysqlUsername(output.getUsername()).mysqlPassword(output.getPassword()).jdbcUrl(output.getJdbcUrl()).schema(output.getSchema()).build());
                        } else {
                            failedUsers.add(User.builder().mysqlUsername(output.getUsername()).jdbcUrl(output.getJdbcUrl()).schema(output.getSchema()).build());
                        }
                    });
                });

                groupNames.forEach(group -> {
                            List<SqlOutput> outputs = databaseService.deleteUsersThatArentInGroup(groupMapping, dbOptions, users);

                            outputs.forEach(output -> {
                                if (output.isDeletedSuccessfully()) {
                                    usersDeleted.add(User.builder().mysqlUsername(output.getUsername()).jdbcUrl(output.getJdbcUrl()).schema(output.getSchema()).build());
                                } else {
                                    failedUsers.add(User.builder().mysqlUsername(output.getUsername()).jdbcUrl(output.getJdbcUrl()).schema(output.getSchema()).build());
                                }
                            });
                        }
                );
            });

            return FunctionOutput.builder().successfulOutputs(usersCreated).deletedOutputs(usersDeleted).failedOutputs(failedUsers).build();
        };
    }

    @Bean
    public Function<FunctionInput, FunctionOutput> resetPassword() {
        log.info("Resetting password");

        Set<User> successfulResets = new HashSet<>();
        Set<User> failedResets = new HashSet<>();

        return value -> {

            List<DatabaseOptions> dbOptions = ssmService.buildDbOptions(value.getParameterStoreKeyInput());

            value.getUsersForPasswordResets().forEach( user -> {
                log.info("Reseting password for {}", user);
                List<SqlOutput> outputs = databaseService.resetUserPassword(user.getSchemas(), dbOptions, user.getUsername());

                outputs.forEach(output -> {
                    if (output.isResetSuccessfully()) {
                        successfulResets.add(User.builder().mysqlUsername(output.getUsername()).mysqlPassword(output.getPassword()).jdbcUrl(output.getJdbcUrl()).schema(output.getSchema()).build());
                    } else {
                        failedResets.add(User.builder().mysqlUsername(output.getUsername()).jdbcUrl(output.getJdbcUrl()).schema(output.getSchema()).build());
                    }
                });
            });


            return FunctionOutput.builder().successfulOutputs(successfulResets).failedOutputs(failedResets).build();
        };
    }

}
