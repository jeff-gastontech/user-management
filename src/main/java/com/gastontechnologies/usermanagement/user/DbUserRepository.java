package com.gastontechnologies.usermanagement.user;

import com.gastontechnologies.usermanagement.database.DatabaseGroupMapping;
import com.gastontechnologies.usermanagement.user.User;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Repository
public class DbUserRepository {

    public void createUser(JdbcTemplate jdbcTemplate, String userName, String password) {
        jdbcTemplate.update("CREATE USER ?@'%' IDENTIFIED BY ?;", userName, password);
    }

    public void updateUserPermissions(JdbcTemplate jdbcTemplate, List<String> permissions, String schema, String username) {
        jdbcTemplate.update(
                "GRANT " + String.join(
                        ",",
                        permissions
                ) + " ON " + schema + ".* TO ?@'%';",
                username
        );

        jdbcTemplate.update("FLUSH PRIVILEGES");
    }

    public void updateUserPassword(JdbcTemplate jdbcTemplate, String username, String password) {
        jdbcTemplate.update("ALTER USER ?@'%' IDENTIFIED BY ?;", username, password);
    }

    public boolean checkIfUserExists(String username, JdbcTemplate jdbcTemplate) {
        return jdbcTemplate.queryForObject("SELECT count(*) FROM user where user = ?;", Integer.class, username) == 1;
    }

    public void dropUser(JdbcTemplate jdbcTemplate, String username) {
        jdbcTemplate.update("DROP USER ?@'%';", username);
    }

    public List<User> findUsers(JdbcTemplate jdbcTemplate, DatabaseGroupMapping databaseGroupMap) {
        return jdbcTemplate.query("select user from user where user like ?", new Object[]{"%." + databaseGroupMap.getGroupName()}, new RowMapper<User>() {
            @Override
            public User mapRow(ResultSet rs, int rowNum) throws SQLException {
                return User.builder().mysqlUsername(rs.getString("user")).build();
            }
        });
    }

}
