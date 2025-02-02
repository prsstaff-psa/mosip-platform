package io.mosip.kernel.masterdata.constant;

public enum LocationErrorCode {
	LOCATION_FETCH_EXCEPTION("KER-MSD-025", "Error occured while fetching Location Hierarchy"),
	LOCATION_NOT_FOUND_EXCEPTION("KER-MSD-026", "Location not found"),
	LOCATION_LEVEL_FETCH_EXCEPTION("KER-MSD-027", "Error occured while fetching Location Hierarchy Levels"),
	LOCATION_INSERT_EXCEPTION("KER-MSD-064", "Error occured while inserting location hierarchy details"),
	LOCATION_UPDATE_EXCEPTION("KER-MSD-097", "Error occured wihile updating location hierarchy details"),
	LOCATION_DELETE_EXCEPTION("KER-MSD-098", "Error occured wihile deleting location hierarchy details"),
	LOCATION_LEVEL_NOT_FOUND_EXCEPTION("KER-MSD-028", "Location Hierarchy Level not found"),
	LOCATION_CHILD_STATUS_EXCEPTION("KER-MSD-300", "Cannot deactivate the Location as active child Location are mapped"), NO_DATA_FOR_FILTER_VALUES("KER-MSD-___","No Data Found for the given filter column");

	private String errorCode;
	private String errorMessage;

	private LocationErrorCode(final String errorCode, final String errorMessage) {
		this.errorCode = errorCode;
		this.errorMessage = errorMessage;
	}

	public String getErrorCode() {
		return errorCode;
	}

	public String getErrorMessage() {
		return errorMessage;
	}
}
