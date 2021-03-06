package io.mosip.kernel.masterdata.exception;

import java.time.format.DateTimeParseException;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import io.mosip.kernel.core.exception.BaseUncheckedException;
import io.mosip.kernel.core.exception.ErrorResponse;
import io.mosip.kernel.core.exception.ServiceError;
import io.mosip.kernel.masterdata.constant.MasterDataConstant;
import io.mosip.kernel.masterdata.constant.RegistrationCenterUserMappingHistoryErrorCode;
import io.mosip.kernel.masterdata.constant.RequestErrorCode;

/**
 * Rest Controller Advice for Master Data
 * 
 * @author Dharmesh Khandelwal
 * @author Bal Vikash Sharma
 *
 * @since 1.0.0
 */
@RestControllerAdvice
public class ApiExceptionHandler {

	@ExceptionHandler(MasterDataServiceException.class)
	public ResponseEntity<ErrorResponse<ServiceError>> controlDataServiceException(final MasterDataServiceException e) {
		return getErrorResponseEntity(e, HttpStatus.INTERNAL_SERVER_ERROR);
	}

	@ExceptionHandler(DataNotFoundException.class)
	public ResponseEntity<ErrorResponse<ServiceError>> controlDataNotFoundException(final DataNotFoundException e) {
		return getErrorResponseEntity(e, HttpStatus.NOT_FOUND);
	}

	@ExceptionHandler(RequestException.class)
	public ResponseEntity<ErrorResponse<ServiceError>> controlRequestException(final RequestException e) {
		return getErrorResponseEntity(e, HttpStatus.BAD_REQUEST);
	}

	@ExceptionHandler(DateTimeParseException.class)
	public ResponseEntity<ErrorResponse<ServiceError>> numberFormatException(final DateTimeParseException e) {
		ServiceError error = new ServiceError(
				RegistrationCenterUserMappingHistoryErrorCode.DATE_TIME_PARSE_EXCEPTION.getErrorCode(),
				e.getMessage() + MasterDataConstant.DATETIMEFORMAT);
		ErrorResponse<ServiceError> errorResponse = new ErrorResponse<>();
		errorResponse.getErrors().add(error);
		return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ErrorResponse<ServiceError>> methodArgumentNotValidException(
			final MethodArgumentNotValidException e) {
		ErrorResponse<ServiceError> errorResponse = new ErrorResponse<>();
		final List<FieldError> fieldErrors = e.getBindingResult().getFieldErrors();
		fieldErrors.forEach(x -> {
			ServiceError error = new ServiceError(RequestErrorCode.REQUEST_DATA_NOT_VALID.getErrorCode(),
					x.getField() + ": " + x.getDefaultMessage());
			errorResponse.getErrors().add(error);
			errorResponse.setStatus(HttpStatus.BAD_REQUEST.value());
		});
		return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
	}

	private ResponseEntity<ErrorResponse<ServiceError>> getErrorResponseEntity(BaseUncheckedException e,
			HttpStatus httpStatus) {
		ServiceError error = new ServiceError(e.getErrorCode(), e.getErrorText());
		ErrorResponse<ServiceError> errorResponse = new ErrorResponse<>();
		errorResponse.getErrors().add(error);
		errorResponse.setStatus(httpStatus.value());
		return new ResponseEntity<>(errorResponse, httpStatus);
	}

}