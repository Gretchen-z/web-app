package org.example.app.service;

import com.google.common.base.Strings;
import lombok.RequiredArgsConstructor;
import org.example.app.domain.RestoreCode;
import org.example.app.domain.User;
import org.example.app.domain.UserWithPassword;
import org.example.app.exception.PasswordNotMatchesException;
import org.example.app.exception.RegistrationException;
import org.example.app.exception.UserNotFoundException;
import org.example.app.exception.WrongAccessException;
import org.example.app.jpa.JpaTransactionTemplate;
import org.example.app.repository.UserRepository;
import org.example.framework.util.KeyValue;
import org.springframework.security.crypto.keygen.StringKeyGenerator;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Collections;
import java.util.Random;

@RequiredArgsConstructor
public class UserService implements AuthenticationProvider, AnonymousProvider {
  private final UserRepository repository;
  private final JpaTransactionTemplate transactionTemplate;
  private final PasswordEncoder passwordEncoder;
  private final StringKeyGenerator keyGenerator;

  @Override
  public Authentication authenticate(Authentication authentication) {
    final var token = (String) authentication.getPrincipal();

    return repository.findByToken(token)
        .map(o -> new TokenAuthentication(o, null, o.getRoles(), true))
        .orElseThrow(AuthenticationException::new);
  }

  @Override
  public AnonymousAuthentication provide() {
    return new AnonymousAuthentication(new User(
        -1,
        "anonymous",
            Collections.emptyList()
    ));
  }

  public RegistrationResponseDto register(RegistrationRequestDto requestDto) {
    // TODO login:
    //  case-sensitivity: coursar Coursar
    //  cleaning: "  Coursar   "
    //  allowed symbols: [A-Za-z0-9]{2,60}
    //  mis...: Admin, Support, root, ...
    //  мат: ...
    // FIXME: check for nullability
    final var username = requestDto.getUsername().trim().toLowerCase();
    // TODO password:
    //  min-length: 8
    //  max-length: 64
    //  non-dictionary
    final var password = requestDto.getPassword().trim();
    final var hash = passwordEncoder.encode(password);
    final var token = keyGenerator.generateKey();
    final var saved = repository.save(0, username, hash).orElseThrow(RegistrationException::new);

    repository.saveToken(saved.getId(), token);
    return new RegistrationResponseDto(saved.getId(), saved.getUsername(), token);
  }

  public LoginResponseDto login(LoginRequestDto requestDto) {
    final var username = requestDto.getUsername().trim().toLowerCase();
    final var password = requestDto.getPassword().trim();


      final var saved = repository.getByUsernameWithPassword(username).orElseThrow(UserNotFoundException::new);

      // TODO: be careful - slow
      if (!passwordEncoder.matches(password, saved.getPassword())) {
        // FIXME: Security issue
        throw new PasswordNotMatchesException();
      }

      final var token = keyGenerator.generateKey();
      repository.saveToken(saved.getId(), token);

    return new LoginResponseDto(saved.getId(), saved.getUsername(), token);
  }


  public RestoreAccountResponseDto restoreCredentials(RestoreAccountRequestDto restoreInfo) {
    boolean isReadyToChangePassword = readyForChangePassword(restoreInfo);

    final var response = new RestoreAccountResponseDto();
    response.setUsername(restoreInfo.getUsername());

    if (isReadyToChangePassword) {
      changeCredentials(restoreInfo);
      response.setMessage("Пароль успешно изменён!");
    } else {
      final var restoreCode = generateRestoreCode(restoreInfo);
      response.setMessage("Мы нашептали вам код на ушко!");
    }

    return response;
  }

  private String generateRestoreCode(RestoreAccountRequestDto restoreInfo) {
    final var username = restoreInfo.getUsername();
    if (Strings.isNullOrEmpty(username)) {
      throw new UserNotFoundException();
    }

    User user = repository.getByUsername(username).orElseThrow(() -> new UserNotFoundException());

    Random rnd = new Random();
    int number = rnd.nextInt(999999);
    final var code = String.format("%06d", number);

    repository.saveRestoreCode(code, user.getId());
    return code;
  }

  private void changeCredentials(RestoreAccountRequestDto restoreInfo) {
    User user = repository.getByUsername(restoreInfo.getUsername()).orElseThrow(() -> new UserNotFoundException());
    RestoreCode restoreCode = repository.getRestoreCodeById(user.getId()).orElseThrow(() -> new RestoreCodeNotFoundException());

    String incomingCode = restoreInfo.getRestoreCode();
    String storedCode = restoreCode.getCode();

    if (incomingCode.equals(storedCode)) {
      final var password = restoreInfo.getNewPassword().trim();
      final var hash = passwordEncoder.encode(password);
      repository.save(user.getId(), user.getUsername(), hash).orElseThrow(RegistrationException::new);
      return;
    }

    throw new WrongAccessException("Неверный код");
  }

  private boolean readyForChangePassword(RestoreAccountRequestDto restoreInfo) {
    return !Strings.isNullOrEmpty(restoreInfo.getRestoreCode())
            && !Strings.isNullOrEmpty(restoreInfo.getNewPassword())
            && !Strings.isNullOrEmpty(restoreInfo.getUsername());
  }
}
