package org.example.app.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.example.framework.security.Authentication;

import java.util.List;

@AllArgsConstructor
@Data
public class UserWithPassword {
  private long id;
  private String username;
  private String password;
  private List<String> roles;
}
