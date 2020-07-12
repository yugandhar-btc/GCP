/*
 * Copyright 2020 Google LLC
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */

package com.google.cloud.healthcare.fdamystudies.beans;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.cloud.healthcare.fdamystudies.common.ErrorCode;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;

@Setter
@Getter
@JsonInclude(Include.NON_NULL)
public class BaseResponse {

  @JsonProperty("status")
  private Integer httpStatusCode;

  @JsonProperty("code")
  private String errorCode;

  @JsonProperty("error_type")
  private String errorType;

  @JsonProperty("error_description")
  private String errorDescription;

  @JsonProperty("message")
  private String message;

  public BaseResponse() {}

  public BaseResponse(ErrorCode errorCode) {
    this.httpStatusCode = errorCode.getStatus();
    this.errorCode = errorCode.getCode();
    this.errorType = errorCode.getErrorType();
    this.errorDescription = errorCode.getDescription();
  }

  public BaseResponse(HttpStatus httpStatus, String message) {
    this.httpStatusCode = httpStatus.value();
    this.message = message;
  }

  @JsonIgnore
  public boolean is2xxSuccessful() {
    return HttpStatus.valueOf(httpStatusCode).is2xxSuccessful();
  }
}
