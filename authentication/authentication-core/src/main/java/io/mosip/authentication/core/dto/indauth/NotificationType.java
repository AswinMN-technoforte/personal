package io.mosip.authentication.core.dto.indauth;

/**
 * 
 * @author Dinesh Karuppiah.T
 */
public enum NotificationType {

	EMAIL("email"), SMS("sms"), NONE("none");

	/**
	 * Variable to hold name
	 */
	private String name;

	NotificationType(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

}
