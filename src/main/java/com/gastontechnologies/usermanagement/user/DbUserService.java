package com.gastontechnologies.usermanagement.user;

import com.gastontechnologies.usermanagement.aws.SSMService;
import com.gastontechnologies.usermanagement.database.DatabaseGroupMapping;
import com.gastontechnologies.usermanagement.database.DatabaseOptions;
import com.gastontechnologies.usermanagement.database.SqlOutput;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.passay.CharacterData;
import org.passay.CharacterRule;
import org.passay.EnglishCharacterData;
import org.passay.PasswordGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
public class DbUserService {

    private static final Logger log = LoggerFactory.getLogger(SSMService.class);

    private HikariDataSource buildDatasource(DatabaseOptions dbConfig) {
        HikariConfig config = new HikariConfig();

        config.setJdbcUrl(dbConfig.getJdbcUrl());
        config.setUsername(dbConfig.getJdbcUsername());
        config.setPassword(dbConfig.getJdbcPassword());
        config.setDriverClassName(dbConfig.getDriverClassName());
        config.setCatalog("mysql");

        return new HikariDataSource(config);
    }

    @Autowired
    private DbUserRepository userRepository;

    public List<SqlOutput> syncUser(User user, DatabaseGroupMapping databaseGroupMap, List<DatabaseOptions> databaseConfigs) {
        List<SqlOutput> outputs = new ArrayList<>();

        for (DatabaseOptions dbConfig : databaseConfigs) {
            try (HikariDataSource ds = buildDatasource(dbConfig)) {

                JdbcTemplate jdbcTemplate = new JdbcTemplate(ds);

                databaseGroupMap.getSchemas().forEach(schema -> {
                    try {

                        boolean exists = userRepository.checkIfUserExists(user.getMysqlUsername(), jdbcTemplate);

                        if (exists) {
                            log.warn("User {} already exists", user.getMysqlUsername());
                            outputs.add(SqlOutput.builder().createdSuccessfully(false).errorMessage("Already exists").username(user.getMysqlUsername()).schema(schema).jdbcUrl(ds.getJdbcUrl()).build());
                        } else {
                            String password = generatePassayPassword();
                            user.setMysqlPassword(password);
                            userRepository.createUser(jdbcTemplate, user.getMysqlUsername(), user.getMysqlPassword());
                        }

                        userRepository.updateUserPermissions(jdbcTemplate, databaseGroupMap.getPermissions(), schema, user.getMysqlUsername());

                        if (!exists) {
                            outputs.add(SqlOutput.builder().createdSuccessfully(true).username(user.getMysqlUsername()).password(user.getMysqlPassword()).schema(schema).jdbcUrl(ds.getJdbcUrl()).build());
                        }
                    } catch (RuntimeException e) {
                        e.printStackTrace();
                        outputs.add(SqlOutput.builder().createdSuccessfully(false).username(user.getMysqlUsername()).schema(schema).errorMessage(e.getMessage()).jdbcUrl(ds.getJdbcUrl()).build());
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return outputs;
    }

    public List<SqlOutput> deleteUsersThatArentInGroup(DatabaseGroupMapping databaseGroupMap, List<DatabaseOptions> databaseConfigs, List<User> usersInGroup) {
        List<SqlOutput> outputs = new ArrayList<>();

        for (DatabaseOptions dbConfig : databaseConfigs) {
            try (HikariDataSource ds = buildDatasource(dbConfig)) {

                JdbcTemplate jdbcTemplate = new JdbcTemplate(ds);

                databaseGroupMap.getSchemas().forEach(schema -> {

                    List<User> allUsers = userRepository.findUsers(jdbcTemplate, databaseGroupMap);

                    log.info("Users in Group: {}", usersInGroup);
                    log.info("All users: {}", allUsers);

                    allUsers.forEach(user -> {
                        log.info("Looking for user: {}", user);
                        try {
                            boolean found = false;
                            for (User knownUser : usersInGroup) {
                                log.info("User: {}", knownUser);
                                if (Objects.equals(user.getMysqlUsername(), knownUser.getMysqlUsername())) {
                                    log.info("Found user!: {}", user.getMysqlUsername());
                                    found = true;
                                    break;
                                }
                            }

                            if (!found) {
                                userRepository.dropUser(jdbcTemplate, user.getMysqlUsername());
                                outputs.add(SqlOutput.builder().deletedSuccessfully(true).username(user.getMysqlUsername()).schema(schema).jdbcUrl(ds.getJdbcUrl()).build());
                                log.info("Deleted user!: {}", user.getMysqlUsername());
                            }
                        } catch (RuntimeException e) {
                            e.printStackTrace();
                            outputs.add(SqlOutput.builder().deletedSuccessfully(false).errorMessage(e.getMessage()).username(user.getMysqlUsername()).schema(schema).jdbcUrl(ds.getJdbcUrl()).build());
                        }
                    });

                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return outputs;
    }

    public List<SqlOutput> resetUserPassword(List<String> schemas, List<DatabaseOptions> databaseConfigs, String username) {
        List<SqlOutput> outputs = new ArrayList<>();
        for (DatabaseOptions dbConfig : databaseConfigs) {
            try (HikariDataSource ds = buildDatasource(dbConfig)) {
                JdbcTemplate jdbcTemplate = new JdbcTemplate(ds);
                schemas.forEach(schema -> {
                    try {
                        String newPassword = generatePassayPassword();

                        userRepository.updateUserPassword(jdbcTemplate, username, newPassword);

                        outputs.add(SqlOutput.builder().username(username).password(newPassword).schema(schema).jdbcUrl(ds.getJdbcUrl()).resetSuccessfully(true).build());
                    } catch (RuntimeException e) {
                        outputs.add(SqlOutput.builder().username(username).schema(schema).jdbcUrl(ds.getJdbcUrl()).resetSuccessfully(false).errorMessage(e.getMessage()).build());
                    }
                });

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return outputs;
    }


    public String generatePassayPassword() {
        PasswordGenerator gen = new PasswordGenerator();
        CharacterData lowerCaseChars = EnglishCharacterData.LowerCase;
        CharacterRule lowerCaseRule = new CharacterRule(lowerCaseChars);
        lowerCaseRule.setNumberOfCharacters(2);

        CharacterData upperCaseChars = EnglishCharacterData.UpperCase;
        CharacterRule upperCaseRule = new CharacterRule(upperCaseChars);
        upperCaseRule.setNumberOfCharacters(2);

        CharacterData digitChars = EnglishCharacterData.Digit;
        CharacterRule digitRule = new CharacterRule(digitChars);
        digitRule.setNumberOfCharacters(2);

        CharacterData specialChars = new CharacterData() {
            public String getErrorCode() {
                return "ERRONEOUS_SPECIAL_CHARS";
            }

            public String getCharacters() {
                return "!@#$%^&*()_+";
            }
        };
        CharacterRule splCharRule = new CharacterRule(specialChars);
        splCharRule.setNumberOfCharacters(2);

        return gen.generatePassword(10, splCharRule, lowerCaseRule,
                upperCaseRule, digitRule);
    }

}
