package org.example.app.repository;

import lombok.RequiredArgsConstructor;
import org.example.app.domain.RestoreCode;
import org.example.app.domain.User;
import org.example.app.domain.UserWithPassword;
import org.example.app.exception.UserNotFoundException;
import org.example.framework.security.Roles;
import org.example.jdbc.JdbcTemplate;
import org.example.jdbc.RowMapper;

import javax.management.relation.Role;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
public class UserRepository {
    private static final long DEFAULT_ROLE_ID = 2;
    private static final String DEFAULT_ROLE_NAME = Roles.ROLE_USER;
    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<List<String>> roleRowMapper = resultSet -> {
      List<String> roles = new ArrayList<>();
      do {
        roles.add(resultSet.getString("roleName"));
      } while (resultSet.next());

      return roles;
  };

    private final RowMapper<UserWithPassword> rowMapperWithPassword = resultSet -> new UserWithPassword(
            resultSet.getLong("id"),
            resultSet.getString("username"),
            resultSet.getString("password"),
            roleRowMapper.mapRow(resultSet)
    );

    private final RowMapper<User> rowMapper = resultSet -> new User(
            resultSet.getLong("id"),
            resultSet.getString("username"),
            roleRowMapper.mapRow(resultSet)
    );

  private final RowMapper<User> rowMapperWithoutRole = resultSet -> new User(
          resultSet.getLong("id"),
          resultSet.getString("username")
  );

    private final RowMapper<RestoreCode> restoreCodeRowMapper = resultSet -> new RestoreCode(
            resultSet.getString("code"),
            resultSet.getLong("userId")
  );

  public Optional<User> getByUsername(String username) {
    // language=PostgreSQL
    return jdbcTemplate.queryOne("SELECT users.id, users.username, roles.\"roleName\"\n" +
            "FROM users, roles\n" +
            "INNER JOIN user_roles ON (roles.id = user_roles.\"roleId\")\n" +
            "WHERE user_roles.\"userId\" = users.id\n" +
            "AND username = ?", rowMapper, username);
  }

  public Optional<UserWithPassword> getByUsernameWithPassword(String username) {
//     language=PostgreSQL
     return jdbcTemplate.queryOne("SELECT users.id, users.username, users.password, roles.\"roleName\"\n" +
             "FROM users, roles\n" +
             "INNER JOIN user_roles ON (roles.id = user_roles.\"roleId\")\n" +
             "WHERE user_roles.\"userId\" = users.id\n" +
             "AND username = ?", rowMapperWithPassword, username);
  }

  /**
   * saves user to db
   *
   * @param id       - user id, if 0 - insert, if not 0 - update
   * @param username
   * @param hash
   */
  // TODO: DuplicateKeyException <-
  public Optional<User> save(long id, String username, String hash) {

     return id == 0 ? saveNewUser(username, hash) : updateUser(id, hash);
  }

    private Optional<User> updateUser(long id, String hash) {
        User user = jdbcTemplate.queryOne(
                """
                        UPDATE users SET password = ? WHERE id = ? RETURNING id, username
                        """,
                rowMapperWithoutRole,
                hash, id
        ).orElseThrow(UserNotFoundException::new);

        List<String> roles = jdbcTemplate.queryAll(
                // language=PostgreSQL
                """
                        SELECT r."roleName"
                         FROM roles r
                         INNER JOIN user_roles ur ON (ur."roleId" = r.id)
                         WHERE ur."userId" = ?
                           """,
                rs -> rs.getString("roleName"), id
        );
        user.setRoles(roles);
        return Optional.of(user);
    }

  private Optional<User> saveNewUser(String username, String hash) {
      User user = jdbcTemplate.queryOne(
              """
                      INSERT INTO users(username, password) VALUES (?, ?) RETURNING id, username
                      """,
              rowMapperWithoutRole,
              username, hash
      ).get();

      saveUserRole(user.getId(), DEFAULT_ROLE_ID);
      user.setRoles(List.of(DEFAULT_ROLE_NAME));
      return Optional.of(user);
  }

  public Optional<User> findByToken(String token) {
    // language=PostgreSQL
    return jdbcTemplate.queryOne(
        """
            SELECT u.id, u.username, r."roleName"
            FROM roles r, users u
                INNER JOIN tokens t ON (t."userId" = u.id)
                INNER JOIN user_roles ur ON (ur."userId" = u.id)
            
            WHERE t.token = ?
            AND ur."roleId" = r.id;
            """,
        rowMapper,
        token
    );
  }

  public void saveToken(long userId, String token) {
    // query - SELECT'ов (ResultSet)
    // update - ? int/long
    // language=PostgreSQL
    jdbcTemplate.update(
        """
            INSERT INTO tokens(token, "userId") VALUES (?, ?)
            """,
        token, userId
    );
  }

  public void saveUserRole(long userId, long roleIs){
      jdbcTemplate.update(
              """
                  INSERT INTO user_roles("roleId", "userId") VALUES (?, ?)
                  """,
              roleIs, userId
      );
  }

  public void saveRestoreCode(String code, long userId) {
    // language=PostgreSQL
    jdbcTemplate.update(
            """
                INSERT INTO restore_codes(code, "userId") VALUES (?, ?)
                """,
            code, userId
    );
  }

  public Optional<RestoreCode> getRestoreCodeById(long userId) {
    // language=PostgreSQL
    return jdbcTemplate.queryOne(
            """
                SELECT "userId", code FROM restore_codes
                WHERE "userId" = ?
                  AND active = true
                ORDER BY created DESC
                LIMIT 1;
                """,
            restoreCodeRowMapper,
            userId
    );
  }
}
