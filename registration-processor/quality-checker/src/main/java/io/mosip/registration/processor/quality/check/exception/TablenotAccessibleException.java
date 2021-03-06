package io.mosip.registration.processor.quality.check.exception;
	
import io.mosip.kernel.core.exception.BaseUncheckedException;
import io.mosip.registration.processor.core.exception.util.PlatformErrorMessages;

/**
 * The Class TablenotAccessibleException.
 */
public class TablenotAccessibleException extends BaseUncheckedException {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;

	/**
	 * Instantiates a new tablenot accessible exception.
	 */
	public TablenotAccessibleException() {
		super();
	}

	/**
	 * Instantiates a new tablenot accessible exception.
	 *
	 * @param errorMessage the error message
	 */
	public TablenotAccessibleException(String errorMessage) {
		super(PlatformErrorMessages.RPR_QCR_INVALID_REGISTRATION_ID.getCode(), errorMessage);
	}

	/**
	 * Instantiates a new tablenot accessible exception.
	 *
	 * @param message the message
	 * @param cause the cause
	 */
	public TablenotAccessibleException(String message, Throwable cause) {
		super(PlatformErrorMessages.RPR_QCR_INVALID_REGISTRATION_ID.getCode(), message, cause);
	}

}