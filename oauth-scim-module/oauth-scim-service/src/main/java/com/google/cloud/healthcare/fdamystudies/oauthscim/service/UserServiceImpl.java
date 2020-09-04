/*
 * Copyright 2020 Google LLC
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */

package com.google.cloud.healthcare.fdamystudies.oauthscim.service;

import static com.google.cloud.healthcare.fdamystudies.common.EncryptionUtils.encrypt;
import static com.google.cloud.healthcare.fdamystudies.common.EncryptionUtils.hash;
import static com.google.cloud.healthcare.fdamystudies.common.EncryptionUtils.salt;
import static com.google.cloud.healthcare.fdamystudies.common.JsonUtils.createArrayNode;
import static com.google.cloud.healthcare.fdamystudies.common.JsonUtils.getObjectNode;
import static com.google.cloud.healthcare.fdamystudies.common.JsonUtils.getTextValue;
import static com.google.cloud.healthcare.fdamystudies.oauthscim.common.AuthScimConstants.ACCOUNT_LOCK_EMAIL_TIMESTAMP;
import static com.google.cloud.healthcare.fdamystudies.oauthscim.common.AuthScimConstants.EXPIRE_TIMESTAMP;
import static com.google.cloud.healthcare.fdamystudies.oauthscim.common.AuthScimConstants.GRANT_TYPE;
import static com.google.cloud.healthcare.fdamystudies.oauthscim.common.AuthScimConstants.HASH;
import static com.google.cloud.healthcare.fdamystudies.oauthscim.common.AuthScimConstants.LOGIN_ATTEMPTS;
import static com.google.cloud.healthcare.fdamystudies.oauthscim.common.AuthScimConstants.LOGIN_TIMESTAMP;
import static com.google.cloud.healthcare.fdamystudies.oauthscim.common.AuthScimConstants.OTP_USED;
import static com.google.cloud.healthcare.fdamystudies.oauthscim.common.AuthScimConstants.PASSWORD;
import static com.google.cloud.healthcare.fdamystudies.oauthscim.common.AuthScimConstants.PASSWORD_HISTORY;
import static com.google.cloud.healthcare.fdamystudies.oauthscim.common.AuthScimConstants.REFRESH_TOKEN;
import static com.google.cloud.healthcare.fdamystudies.oauthscim.common.AuthScimConstants.SALT;
import static com.google.cloud.healthcare.fdamystudies.oauthscim.common.AuthScimConstants.TEMP_PASSWORD_LENGTH;
import static com.google.cloud.healthcare.fdamystudies.oauthscim.common.AuthScimConstants.TOKEN;
import static com.google.cloud.healthcare.fdamystudies.oauthscim.common.AuthScimEvent.ACCOUNT_LOCKED;
import static com.google.cloud.healthcare.fdamystudies.oauthscim.common.AuthScimEvent.CLIENT_CREDENTIAL_VALIDATION_SUCCEEDED;
import static com.google.cloud.healthcare.fdamystudies.oauthscim.common.AuthScimEvent.INVALID_CLIENT_APPLICATION_CREDENTIALS;
import static com.google.cloud.healthcare.fdamystudies.oauthscim.common.AuthScimEvent.INVALID_CLIENT_ID_OR_SECRET;
import static com.google.cloud.healthcare.fdamystudies.oauthscim.common.AuthScimEvent.INVALID_REFRESH_TOKEN;
import static com.google.cloud.healthcare.fdamystudies.oauthscim.common.AuthScimEvent.NEW_ACCESS_TOKEN_GENERATION_FAILED_INVALID_CLIENT_CREDENTIALS;
import static com.google.cloud.healthcare.fdamystudies.oauthscim.common.AuthScimEvent.NEW_ACCESS_TOKEN_GENERATION_FAILED_INVALID_GRANT_TYPE;
import static com.google.cloud.healthcare.fdamystudies.oauthscim.common.AuthScimEvent.PASSWORD_CHANGE_FAILED;
import static com.google.cloud.healthcare.fdamystudies.oauthscim.common.AuthScimEvent.PASSWORD_CHANGE_SUCCEEDED;
import static com.google.cloud.healthcare.fdamystudies.oauthscim.common.AuthScimEvent.PASSWORD_HELP_EMAIL_FAILED;
import static com.google.cloud.healthcare.fdamystudies.oauthscim.common.AuthScimEvent.PASSWORD_HELP_EMAIL_SENT;
import static com.google.cloud.healthcare.fdamystudies.oauthscim.common.AuthScimEvent.PASSWORD_HELP_REQUESTED;
import static com.google.cloud.healthcare.fdamystudies.oauthscim.common.AuthScimEvent.PASSWORD_HELP_REQUESTED_FOR_UNREGISTERED_USERNAME;
import static com.google.cloud.healthcare.fdamystudies.oauthscim.common.AuthScimEvent.PASSWORD_RESET_EMAIL_FAILED_FOR_LOCKED_ACCOUNT;
import static com.google.cloud.healthcare.fdamystudies.oauthscim.common.AuthScimEvent.PASSWORD_RESET_EMAIL_SENT_FOR_LOCKED_ACCOUNT;
import static com.google.cloud.healthcare.fdamystudies.oauthscim.common.AuthScimEvent.PASSWORD_RESET_FAILED;
import static com.google.cloud.healthcare.fdamystudies.oauthscim.common.AuthScimEvent.PASSWORD_RESET_SUCCEEDED;
import static com.google.cloud.healthcare.fdamystudies.oauthscim.common.AuthScimEvent.SIGNIN_FAILED;
import static com.google.cloud.healthcare.fdamystudies.oauthscim.common.AuthScimEvent.SIGNIN_FAILED_EXPIRED_PASSWORD;
import static com.google.cloud.healthcare.fdamystudies.oauthscim.common.AuthScimEvent.SIGNIN_FAILED_EXPIRED_TEMPORARY_PASSWORD;
import static com.google.cloud.healthcare.fdamystudies.oauthscim.common.AuthScimEvent.SIGNIN_FAILED_INVALID_PASSWORD;
import static com.google.cloud.healthcare.fdamystudies.oauthscim.common.AuthScimEvent.SIGNIN_FAILED_INVALID_TEMPORARY_PASSWORD;
import static com.google.cloud.healthcare.fdamystudies.oauthscim.common.AuthScimEvent.SIGNIN_FAILED_UNREGISTERED_USER;
import static com.google.cloud.healthcare.fdamystudies.oauthscim.common.AuthScimEvent.SIGNIN_SUCCEEDED;
import static com.google.cloud.healthcare.fdamystudies.oauthscim.common.AuthScimEvent.SIGNIN_WITH_TEMPORARY_PASSWORD_FAILED;
import static com.google.cloud.healthcare.fdamystudies.oauthscim.common.AuthScimEvent.SIGNIN_WITH_TEMPORARY_PASSWORD_SUCCEEDED;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.cloud.healthcare.fdamystudies.beans.AuditLogEventRequest;
import com.google.cloud.healthcare.fdamystudies.beans.AuthenticationResponse;
import com.google.cloud.healthcare.fdamystudies.beans.ChangePasswordRequest;
import com.google.cloud.healthcare.fdamystudies.beans.ChangePasswordResponse;
import com.google.cloud.healthcare.fdamystudies.beans.EmailRequest;
import com.google.cloud.healthcare.fdamystudies.beans.EmailResponse;
import com.google.cloud.healthcare.fdamystudies.beans.ResetPasswordRequest;
import com.google.cloud.healthcare.fdamystudies.beans.ResetPasswordResponse;
import com.google.cloud.healthcare.fdamystudies.beans.UpdateEmailStatusRequest;
import com.google.cloud.healthcare.fdamystudies.beans.UpdateEmailStatusResponse;
import com.google.cloud.healthcare.fdamystudies.beans.UserRequest;
import com.google.cloud.healthcare.fdamystudies.beans.UserResponse;
import com.google.cloud.healthcare.fdamystudies.common.DateTimeUtils;
import com.google.cloud.healthcare.fdamystudies.common.ErrorCode;
import com.google.cloud.healthcare.fdamystudies.common.IdGenerator;
import com.google.cloud.healthcare.fdamystudies.common.MessageCode;
import com.google.cloud.healthcare.fdamystudies.common.PasswordGenerator;
import com.google.cloud.healthcare.fdamystudies.common.TextEncryptor;
import com.google.cloud.healthcare.fdamystudies.common.UserAccountStatus;
import com.google.cloud.healthcare.fdamystudies.exceptions.ErrorCodeException;
import com.google.cloud.healthcare.fdamystudies.oauthscim.common.AuthScimAuditHelper;
import com.google.cloud.healthcare.fdamystudies.oauthscim.config.AppPropertyConfig;
import com.google.cloud.healthcare.fdamystudies.oauthscim.mapper.UserMapper;
import com.google.cloud.healthcare.fdamystudies.oauthscim.model.UserEntity;
import com.google.cloud.healthcare.fdamystudies.oauthscim.repository.UserRepository;
import com.google.cloud.healthcare.fdamystudies.service.EmailService;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.ext.XLogger;
import org.slf4j.ext.XLoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

@Service
public class UserServiceImpl implements UserService {

  private XLogger logger = XLoggerFactory.getXLogger(UserServiceImpl.class.getName());

  @Autowired private UserRepository repository;

  @Autowired private AppPropertyConfig appConfig;

  @Autowired private EmailService emailService;

  @Autowired private AuthScimAuditHelper auditHelper;

  @Autowired private OAuthService oauthService;

  @Autowired private TextEncryptor encryptor;

  @Override
  @Transactional
  public UserResponse createUser(UserRequest userRequest) {
    logger.entry("begin createUser()");

    // check if the email already been used
    Optional<UserEntity> user =
        repository.findByAppIdAndEmail(userRequest.getAppId(), userRequest.getEmail());

    if (user.isPresent()) {
      throw new ErrorCodeException(ErrorCode.EMAIL_EXISTS);
    }

    // save user account details
    UserEntity userEntity = UserMapper.fromUserRequest(userRequest);
    ObjectNode userInfo = getObjectNode();
    setPasswordAndPasswordHistoryFields(
        userRequest.getPassword(), userInfo, UserAccountStatus.PENDING_CONFIRMATION.getStatus());

    userEntity.setUserInfo(userInfo);
    userEntity = repository.saveAndFlush(userEntity);

    logger.exit(String.format("id=%s", userEntity.getId()));
    return UserMapper.toUserResponse(userEntity);
  }

  private void setPasswordAndPasswordHistoryFields(
      String password, JsonNode userInfoJsonNode, int accountStatus) {
    // encrypt the password using random salt
    String rawSalt = salt();
    String encrypted = encrypt(password, rawSalt);

    ObjectNode passwordNode = getObjectNode();
    passwordNode.put(HASH, hash(encrypted));
    passwordNode.put(SALT, rawSalt);

    UserAccountStatus userAccountStatus = UserAccountStatus.valueOf(accountStatus);
    switch (userAccountStatus) {
      case ACCOUNT_LOCKED:
        passwordNode.put(
            EXPIRE_TIMESTAMP,
            DateTimeUtils.getSystemDateTimestamp(0, 0, appConfig.getAccountLockPeriodInMinutes()));
        break;
      case PASSWORD_RESET:
        passwordNode.put(
            EXPIRE_TIMESTAMP,
            DateTimeUtils.getSystemDateTimestamp(0, appConfig.getResetPasswordExpiryInHours(), 0));
        break;
      default:
        passwordNode.put(
            EXPIRE_TIMESTAMP,
            DateTimeUtils.getSystemDateTimestamp(appConfig.getPasswordExpiryDays(), 0, 0));
    }

    ObjectNode userInfo = (ObjectNode) userInfoJsonNode;
    ArrayNode passwordHistory =
        userInfo.hasNonNull(PASSWORD_HISTORY)
            ? (ArrayNode) userInfo.get(PASSWORD_HISTORY)
            : createArrayNode();
    passwordHistory.add(passwordNode);

    // keep only 'X' previous passwords
    logger.trace(String.format("password history has %d elements", passwordHistory.size()));
    while (passwordHistory.size() > appConfig.getPasswordHistoryMaxSize()) {
      passwordHistory.remove(0);
    }

    userInfo.set(PASSWORD, passwordNode);
    userInfo.set(PASSWORD_HISTORY, passwordHistory);
  }

  @Override
  public ResetPasswordResponse resetPassword(
      ResetPasswordRequest resetPasswordRequest, AuditLogEventRequest auditRequest)
      throws JsonProcessingException {
    logger.entry("begin resetPassword()");
    UserEntity userEntity = null;
    Optional<UserEntity> entity =
        repository.findByAppIdAndEmail(
            resetPasswordRequest.getAppId(), resetPasswordRequest.getEmail());
    Optional<UserEntity> optionalEntity = repository.findByUserId(resetPasswordRequest.getUserId());
    if (optionalEntity.isPresent()) {
      userEntity = optionalEntity.get();
    }
    if (!entity.isPresent()) {
      auditHelper.logEvent(PASSWORD_RESET_FAILED, auditRequest);
      auditHelper.logEvent(PASSWORD_HELP_EMAIL_FAILED, auditRequest);
      auditHelper.logEvent(PASSWORD_HELP_REQUESTED_FOR_UNREGISTERED_USERNAME, auditRequest);
      if (UserAccountStatus.ACCOUNT_LOCKED.equals(
          UserAccountStatus.valueOf(userEntity.getStatus())))
        auditHelper.logEvent(PASSWORD_RESET_EMAIL_FAILED_FOR_LOCKED_ACCOUNT, auditRequest);
      logger.exit(String.format("reset password failed, error code=%s", ErrorCode.USER_NOT_FOUND));
      throw new ErrorCodeException(ErrorCode.USER_NOT_FOUND);
    }
    auditHelper.logEvent(PASSWORD_HELP_REQUESTED, auditRequest);

    if (userEntity.getStatus() == UserAccountStatus.PENDING_CONFIRMATION.getStatus()) {
      throw new ErrorCodeException(ErrorCode.ACCOUNT_NOT_VERIFIED);
    }

    if (userEntity.getStatus() == UserAccountStatus.DEACTIVATED.getStatus()) {
      throw new ErrorCodeException(ErrorCode.ACCOUNT_DEACTIVATED);
    }

    String tempPassword = PasswordGenerator.generate(TEMP_PASSWORD_LENGTH);
    EmailResponse emailResponse = sendPasswordResetEmail(resetPasswordRequest, tempPassword);
    auditHelper.logEvent(PASSWORD_HELP_EMAIL_SENT, auditRequest);
    if (UserAccountStatus.ACCOUNT_LOCKED.equals(UserAccountStatus.valueOf(userEntity.getStatus())))
      auditHelper.logEvent(PASSWORD_RESET_EMAIL_SENT_FOR_LOCKED_ACCOUNT, auditRequest);

    if (HttpStatus.ACCEPTED.value() == emailResponse.getHttpStatusCode()) {

      ObjectNode userInfo = (ObjectNode) userEntity.getUserInfo();
      setPasswordAndPasswordHistoryFields(tempPassword, userInfo, userEntity.getStatus());
      userEntity.setUserInfo(userInfo);
      repository.saveAndFlush(userEntity);
      logger.exit(MessageCode.PASSWORD_RESET_SUCCESS);
      auditHelper.logEvent(PASSWORD_RESET_SUCCEEDED, auditRequest);
      return new ResetPasswordResponse(MessageCode.PASSWORD_RESET_SUCCESS);
    }
    logger.exit(
        String.format(
            "reset password failed, error code=%s", ErrorCode.EMAIL_SEND_FAILED_EXCEPTION));
    throw new ErrorCodeException(ErrorCode.EMAIL_SEND_FAILED_EXCEPTION);
  }

  private EmailResponse sendPasswordResetEmail(
      ResetPasswordRequest resetPasswordRequest, String tempPassword) {
    Map<String, String> templateArgs = new HashMap<>();
    templateArgs.put("appId", resetPasswordRequest.getAppId());
    templateArgs.put("contactEmail", appConfig.getContactEmail());
    templateArgs.put("tempPassword", tempPassword);
    EmailRequest emailRequest =
        new EmailRequest(
            appConfig.getFromEmail(),
            new String[] {resetPasswordRequest.getEmail()},
            null,
            null,
            appConfig.getMailResetPasswordSubject(),
            appConfig.getMailResetPasswordBody(),
            templateArgs);
    return emailService.sendMimeMail(emailRequest);
  }

  public ChangePasswordResponse changePassword(
      ChangePasswordRequest userRequest, AuditLogEventRequest auditRequest)
      throws JsonProcessingException {
    logger.entry("begin changePassword()");
    Optional<UserEntity> optionalEntity = repository.findByUserId(userRequest.getUserId());

    if (!optionalEntity.isPresent()) {
      throw new ErrorCodeException(ErrorCode.USER_NOT_FOUND);
    }

    UserEntity userEntity = optionalEntity.get();
    userEntity.setStatus(UserAccountStatus.ACTIVE.getStatus());
    ObjectNode userInfo = (ObjectNode) userEntity.getUserInfo();
    ArrayNode passwordHistory =
        userInfo.hasNonNull(PASSWORD_HISTORY)
            ? (ArrayNode) userInfo.get(PASSWORD_HISTORY)
            : createArrayNode();
    JsonNode currentPwdNode = userInfo.get(PASSWORD);

    ErrorCode errorCode =
        validateChangePasswordRequest(userRequest, currentPwdNode, passwordHistory);
    if (errorCode != null) {
      if (auditRequest != null) auditHelper.logEvent(PASSWORD_CHANGE_FAILED, auditRequest);
      throw new ErrorCodeException(errorCode);
    }

    auditHelper.logEvent(PASSWORD_CHANGE_SUCCEEDED, auditRequest);
    setPasswordAndPasswordHistoryFields(
        userRequest.getNewPassword(), userInfo, userEntity.getStatus());
    userEntity.setUserInfo(userInfo);
    repository.saveAndFlush(userEntity);
    logger.exit("Your password has been changed successfully!");
    return new ChangePasswordResponse(MessageCode.CHANGE_PASSWORD_SUCCESS);
  }

  private ErrorCode validateChangePasswordRequest(
      ChangePasswordRequest userRequest, JsonNode passwordNode, ArrayNode passwordHistory) {
    // determine whether the current password matches the password stored in database
    String hash = getTextValue(passwordNode, HASH);
    String rawSalt = getTextValue(passwordNode, SALT);
    String currentPasswordHash = hash(encrypt(userRequest.getCurrentPassword(), rawSalt));
    if (!StringUtils.equals(currentPasswordHash, hash)) {
      return ErrorCode.CURRENT_PASSWORD_INVALID;
    }

    // evaluate whether the new password matches any of the previous passwords
    String prevPasswordHash;
    String salt;
    for (JsonNode pwd : passwordHistory) {
      salt = getTextValue(pwd, SALT);
      prevPasswordHash = getTextValue(pwd, HASH);
      String newPasswordHash = hash(encrypt(userRequest.getNewPassword(), salt));
      if (StringUtils.equals(prevPasswordHash, newPasswordHash)) {
        return ErrorCode.ENFORCE_PASSWORD_HISTORY;
      }
    }
    return null;
  }

  @Override
  public Optional<UserEntity> findUserByTempRegId(String tempRegId) {
    return repository.findByTempRegId(tempRegId);
  }

  @Override
  @Transactional(noRollbackFor = ErrorCodeException.class)
  public AuthenticationResponse authenticate(UserRequest user, AuditLogEventRequest auditRequest)
      throws JsonProcessingException {
    logger.entry("begin authenticate(user)");
    // check if the email present in the database
    Optional<UserEntity> optUserEntity =
        repository.findByAppIdAndEmail(user.getAppId(), user.getEmail());

    if (!optUserEntity.isPresent()) {
      // log SIGNIN_FAILED_UNREGISTERED_USER event
      auditHelper.logEvent(SIGNIN_FAILED_UNREGISTERED_USER, auditRequest);
      throw new ErrorCodeException(ErrorCode.USER_NOT_FOUND);
    }

    UserEntity userEntity = optUserEntity.get();
    JsonNode userInfo = userEntity.getUserInfo();

    JsonNode passwordNode = userInfo.get(PASSWORD);
    String hash = getTextValue(passwordNode, HASH);
    String salt = getTextValue(passwordNode, SALT);

    // check the account status and password expiry condition
    ErrorCode errorCode =
        validatePasswordExpiryAndAccountStatus(userEntity, userInfo, auditRequest);
    Map<String, String> userIdPH = Collections.singletonMap("user_id", userEntity.getUserId());
    Map<String, String> systemIdPH =
        Collections.singletonMap("resource_requesting_entity_systemid", userEntity.getAppId());
    if (errorCode == null) {
      auditHelper.logEvent(CLIENT_CREDENTIAL_VALIDATION_SUCCEEDED, auditRequest, systemIdPH);
      String passwordHash = hash(encrypt(user.getPassword(), salt));
      if (StringUtils.equals(passwordHash, hash)) {
        // reset login attempts
        return updateLoginAttemptsAndAuthenticationTime(userEntity, userInfo, auditRequest);
      } else {
        auditHelper.logEvent(INVALID_CLIENT_APPLICATION_CREDENTIALS, auditRequest, userIdPH);
        auditHelper.logEvent(INVALID_CLIENT_ID_OR_SECRET, auditRequest, systemIdPH);
        errorCode = ErrorCode.INVALID_LOGIN_CREDENTIALS;
      }
    }

    // increment login attempts
    return updateInvalidLoginAttempts(userEntity, userInfo, errorCode, auditRequest);
  }

  private EmailResponse sendAccountLockedEmail(UserEntity user, String tempPassword) {
    logger.entry("sendAccountLockedEmail()");
    Map<String, String> templateArgs = new HashMap<>();
    templateArgs.put("appId", user.getAppId());
    templateArgs.put("contactEmail", appConfig.getContactEmail());
    templateArgs.put("tempPassword", tempPassword);
    EmailRequest emailRequest =
        new EmailRequest(
            appConfig.getFromEmail(),
            new String[] {user.getEmail()},
            null,
            null,
            appConfig.getMailAccountLockedSubject(),
            appConfig.getMailAccountLockedBody(),
            templateArgs);
    EmailResponse emailResponse = emailService.sendMimeMail(emailRequest);
    logger.exit(
        String.format("send account locked email status=%d", emailResponse.getHttpStatusCode()));
    return emailResponse;
  }

  private AuthenticationResponse updateInvalidLoginAttempts(
      UserEntity userEntity,
      JsonNode userInfoJsonNode,
      ErrorCode errorCode,
      AuditLogEventRequest auditRequest) {

    if (userEntity.getStatus() == UserAccountStatus.PASSWORD_RESET.getStatus()
        || userEntity.getStatus() == UserAccountStatus.ACCOUNT_LOCKED.getStatus()) {
      // log SIGNIN_WITH_TEMPORARY_PASSWORD_FAILED audit log event
      auditHelper.logEvent(SIGNIN_WITH_TEMPORARY_PASSWORD_FAILED, auditRequest);
      auditHelper.logEvent(SIGNIN_FAILED_INVALID_TEMPORARY_PASSWORD, auditRequest);
    } else {
      // log SIGNIN_FAILED_INVALID_PASSWORD audit log event
      auditHelper.logEvent(SIGNIN_FAILED_INVALID_PASSWORD, auditRequest);
    }

    // log SIGNIN_FAILED audit log event
    auditHelper.logEvent(SIGNIN_FAILED, auditRequest);

    ObjectNode userInfo = (ObjectNode) userInfoJsonNode;
    int loginAttempts =
        userInfo.hasNonNull(LOGIN_ATTEMPTS) ? userInfo.get(LOGIN_ATTEMPTS).intValue() : 0;
    loginAttempts += 1;
    userInfo.put(LOGIN_ATTEMPTS, loginAttempts);

    if (userInfo.get(LOGIN_ATTEMPTS).intValue() >= appConfig.getMaxInvalidLoginAttempts()) {
      String tempPassword = PasswordGenerator.generate(12);
      setPasswordAndPasswordHistoryFields(
          tempPassword, userInfo, UserAccountStatus.ACCOUNT_LOCKED.getStatus());
      sendAccountLockedEmail(userEntity, tempPassword);
      userEntity.setStatus(UserAccountStatus.ACCOUNT_LOCKED.getStatus());
      userInfo.put(ACCOUNT_LOCK_EMAIL_TIMESTAMP, Instant.now().toEpochMilli());

      Map<String, String> placeHolders =
          Stream.of(
                  new String[][] {
                    {"lock_time", Instant.now().toString()},
                    {"failed_attempts", String.valueOf(loginAttempts)}
                  })
              .collect(Collectors.toMap(data -> data[0], data -> data[1]));
      auditHelper.logEvent(ACCOUNT_LOCKED, auditRequest, placeHolders);
    }

    userEntity.setUserInfo(userInfo);
    userEntity = repository.saveAndFlush(userEntity);

    errorCode =
        UserAccountStatus.ACCOUNT_LOCKED.equals(UserAccountStatus.valueOf(userEntity.getStatus()))
            ? ErrorCode.ACCOUNT_LOCKED
            : errorCode;

    throw new ErrorCodeException(errorCode);
  }

  private AuthenticationResponse updateLoginAttemptsAndAuthenticationTime(
      UserEntity userEntity, JsonNode userInfoJsonNode, AuditLogEventRequest auditRequest) {
    ObjectNode passwordNode = (ObjectNode) userInfoJsonNode.get(PASSWORD);
    UserAccountStatus status = UserAccountStatus.valueOf(userEntity.getStatus());
    passwordNode.remove(EXPIRE_TIMESTAMP);
    if (UserAccountStatus.PASSWORD_RESET.equals(status)
        || UserAccountStatus.ACCOUNT_LOCKED.equals(status)) {

      // log TEMPORARY_PASSWORD_SUCCEEDED audit log event
      auditHelper.logEvent(SIGNIN_WITH_TEMPORARY_PASSWORD_SUCCEEDED, auditRequest);
      passwordNode.remove(EXPIRE_TIMESTAMP);
      passwordNode.put(OTP_USED, true);
    } else {
      auditHelper.logEvent(SIGNIN_WITH_TEMPORARY_PASSWORD_FAILED, auditRequest);
      passwordNode.remove(OTP_USED);
    }

    ObjectNode userInfo = (ObjectNode) userInfoJsonNode;
    userInfo.remove(ACCOUNT_LOCK_EMAIL_TIMESTAMP);
    userInfo.set(PASSWORD, passwordNode);
    userInfo.put(LOGIN_ATTEMPTS, 0);
    userInfo.put(LOGIN_TIMESTAMP, Instant.now().toEpochMilli());

    userEntity.setUserInfo(userInfo);
    userEntity = repository.saveAndFlush(userEntity);

    // log SIGNIN_SUCCEEDED audit log event
    auditHelper.logEvent(SIGNIN_SUCCEEDED, auditRequest);
    AuthenticationResponse authenticationResponse = new AuthenticationResponse();
    authenticationResponse.setUserId(userEntity.getUserId());
    authenticationResponse.setAccountStatus(userEntity.getStatus());
    authenticationResponse.setHttpStatusCode(HttpStatus.OK.value());
    return authenticationResponse;
  }

  public ErrorCode validatePasswordExpiryAndAccountStatus(
      UserEntity userEntity, JsonNode userInfo, AuditLogEventRequest auditRequest) {
    JsonNode passwordNode = userInfo.get(PASSWORD);
    UserAccountStatus accountStatus = UserAccountStatus.valueOf(userEntity.getStatus());
    switch (accountStatus) {
      case DEACTIVATED:
        return ErrorCode.ACCOUNT_DEACTIVATED;
      case ACCOUNT_LOCKED:
        auditHelper.logEvent(SIGNIN_FAILED_EXPIRED_TEMPORARY_PASSWORD, auditRequest);
        return isPasswordExpired(passwordNode) ? ErrorCode.TEMP_PASSWORD_EXPIRED : null;
      case PASSWORD_RESET:
        auditHelper.logEvent(SIGNIN_FAILED_EXPIRED_PASSWORD, auditRequest);
        return isPasswordExpired(passwordNode) ? ErrorCode.TEMP_PASSWORD_EXPIRED : null;
      default:
        return isPasswordExpired(passwordNode) ? ErrorCode.PASSWORD_EXPIRED : null;
    }
  }

  private boolean isPasswordExpired(JsonNode passwordNode) {
    return (passwordNode.hasNonNull(EXPIRE_TIMESTAMP)
            && Instant.now().toEpochMilli() > passwordNode.get(EXPIRE_TIMESTAMP).longValue()
        || passwordNode.hasNonNull(OTP_USED) && passwordNode.get(OTP_USED).booleanValue());
  }

  @Override
  public void resetTempRegId(String userId) {
    repository.removeTempRegIDForUser(userId);
  }

  @Override
  @Transactional
  public void removeExpiredTempRegIds() {
    long timeInMillis =
        Instant.now()
            .minus(appConfig.getTempRegIdExpiryMinutes(), ChronoUnit.MINUTES)
            .toEpochMilli();
    repository.removeTempRegIdBeforeTime(new Timestamp(timeInMillis));
  }

  @Transactional
  public UpdateEmailStatusResponse updateEmailStatusAndTempRegId(
      UpdateEmailStatusRequest userRequest) throws JsonProcessingException {
    logger.entry("begin updateEmailStatusAndTempRegId()");
    Optional<UserEntity> optUser = repository.findByUserId(userRequest.getUserId());
    if (!optUser.isPresent()) {
      throw new ErrorCodeException(ErrorCode.USER_NOT_FOUND);
    }

    UserEntity userEntity = optUser.get();
    Integer status =
        userRequest.getStatus() == null ? userEntity.getStatus() : userRequest.getStatus();
    String email = StringUtils.defaultIfEmpty(userRequest.getEmail(), userEntity.getEmail());

    String tempRegId = null;
    if (userRequest.getStatus() != null
        && UserAccountStatus.ACTIVE.getStatus() == userRequest.getStatus()) {
      tempRegId = IdGenerator.id();
    }
    repository.updateEmailStatusAndTempRegId(email, status, tempRegId, userEntity.getUserId());
    logger.exit(MessageCode.UPDATE_USER_DETAILS_SUCCESS);
    return new UpdateEmailStatusResponse(MessageCode.UPDATE_USER_DETAILS_SUCCESS, tempRegId);
  }

  @Override
  public UserResponse logout(String userId, AuditLogEventRequest auditRequest)
      throws JsonProcessingException {
    Optional<UserEntity> optUserEntity = repository.findByUserId(userId);

    if (!optUserEntity.isPresent()) {
      throw new ErrorCodeException(ErrorCode.USER_NOT_FOUND);
    }

    return revokeAndReplaceRefreshToken(userId, null, auditRequest);
  }

  @Override
  public UserResponse revokeAndReplaceRefreshToken(
      String userId, String refreshToken, AuditLogEventRequest auditRequest)
      throws JsonProcessingException {
    logger.entry("revokeAndReplaceRefreshToken(userId, refreshToken)");
    Optional<UserEntity> optUserEntity = repository.findByUserId(userId);
    Map<String, String> grantTypePH = Collections.singletonMap("grant_type", GRANT_TYPE);
    if (!optUserEntity.isPresent()) {
      throw new ErrorCodeException(ErrorCode.USER_NOT_FOUND);
    }
    UserEntity userEntity = optUserEntity.get();
    ObjectNode userInfo = (ObjectNode) userEntity.getUserInfo();

    if (userInfo.hasNonNull(REFRESH_TOKEN)) {
      String prevRefreshToken = getTextValue(userInfo, REFRESH_TOKEN);
      prevRefreshToken = encryptor.decrypt(prevRefreshToken);
      HttpHeaders headers = new HttpHeaders();
      headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
      MultiValueMap<String, String> requestParams = new LinkedMultiValueMap<>();
      requestParams.add(TOKEN, prevRefreshToken);
      ResponseEntity<JsonNode> response = oauthService.revokeToken(requestParams, headers);
      if (!response.getStatusCode().is2xxSuccessful()) {
        auditHelper.logEvent(
            NEW_ACCESS_TOKEN_GENERATION_FAILED_INVALID_GRANT_TYPE, auditRequest, grantTypePH);
        auditHelper.logEvent(
            NEW_ACCESS_TOKEN_GENERATION_FAILED_INVALID_CLIENT_CREDENTIALS, auditRequest);
        throw new ErrorCodeException(ErrorCode.APPLICATION_ERROR);
      }
    }

    if (StringUtils.isEmpty(refreshToken)) {
      auditHelper.logEvent(INVALID_REFRESH_TOKEN, auditRequest);
      userInfo.remove(REFRESH_TOKEN);
    } else {
      userInfo.put(REFRESH_TOKEN, encryptor.encrypt(refreshToken));
    }

    userEntity.setUserInfo(userInfo);
    repository.saveAndFlush(userEntity);

    UserResponse userResponse = new UserResponse();
    userResponse.setHttpStatusCode(HttpStatus.OK.value());
    logger.exit(
        "previous refresh token revoked and replaced with new refresh token for the given user");
    return userResponse;
  }

  @Override
  public void deleteUserAccount(String userId) {
    logger.entry("deleteUserAccount");
    Optional<UserEntity> optUserEntity = repository.findByUserId(userId);

    if (!optUserEntity.isPresent()) {
      throw new ErrorCodeException(ErrorCode.USER_NOT_FOUND);
    }

    repository.delete(optUserEntity.get());

    logger.exit("user account deleted");
  }
}
