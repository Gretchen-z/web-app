package org.example.app.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.example.framework.security.Authentication;

import java.util.List;

@AllArgsConstructor
@Data
public class User {
  private long id;
  private String username;
  private List<String> roles;

  public User(long id, String username) {
    this.id = id;
    this.username = username;
  }
}
