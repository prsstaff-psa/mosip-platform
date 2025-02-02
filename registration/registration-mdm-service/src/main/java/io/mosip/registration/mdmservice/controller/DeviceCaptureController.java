package io.mosip.registration.mdmservice.controller;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.mosip.registration.mdmservice.constants.MosipBioDeviceConstants;
import io.mosip.registration.mdmservice.dto.CaptureResponseData;
import io.mosip.registration.mdmservice.dto.MosipBioCaptureRequestDto;
import io.mosip.registration.mdmservice.dto.MosipBioCaptureResponse;
import io.mosip.registration.mdmservice.dto.MosipBioCaptureResponseDto;
import io.mosip.registration.mdmservice.dto.MosipBioRequest;

/**
 * @author Taleev Aalam The Class is the controller for device capture rest call
 */
@RestController
public class DeviceCaptureController {

	/**
	 * Returns the captured data
	 * 
	 * @param MosipBioCaptureRequestDto
	 *            -mosipBioCaptureRequestDto
	 * @return MosipBioCaptureResponseDto
	 * 
	 */
	@PostMapping("/capture")
	public MosipBioCaptureResponseDto getCaputuredData(@RequestBody MosipBioCaptureRequestDto requestDto) {

		MosipBioRequest mosipBioRequest = requestDto.getMosipBioRequest().get(0);

		String deviceId = mosipBioRequest.getDeviceId();
		String deviceSubId = mosipBioRequest.getDeviceSubId();
		String bioType = deviceId;
		if (deviceSubId != null && !deviceSubId.isEmpty()) {
			bioType = deviceId + "_" + deviceSubId;
		}

		MosipBioCaptureResponseDto mosipBioCaptureResponseDto = new MosipBioCaptureResponseDto();
		List<MosipBioCaptureResponse> mosipBioCaptureResponses = new ArrayList<MosipBioCaptureResponse>();

		mosipBioCaptureResponseDto.setMosipBioDeviceDataResponses(mosipBioCaptureResponses);
		switch (bioType) {
		case MosipBioDeviceConstants.VALUE_FINGERPRINT + "_" + MosipBioDeviceConstants.VALUE_SINGLE:
			stubImage(mosipBioCaptureResponses, mosipBioRequest, bioType);
			break;
		case MosipBioDeviceConstants.VALUE_FINGERPRINT + "_" + MosipBioDeviceConstants.VALUE_SLAP_LEFT:

			mosipBioCaptureResponseDto.setSlapImage(getCapturedByte(bioType));
			stubSegmentedBiometrics(mosipBioRequest, mosipBioCaptureResponses,
					MosipBioDeviceConstants.LEFTHAND_SEGMNTD_FILE_PATHS);

			break;
		case MosipBioDeviceConstants.VALUE_FINGERPRINT + "_" + MosipBioDeviceConstants.VALUE_SLAP_RIGHT:
			mosipBioCaptureResponseDto.setSlapImage(getCapturedByte(bioType));
			stubSegmentedBiometrics(mosipBioRequest, mosipBioCaptureResponses,
					MosipBioDeviceConstants.RIGHTHAND_SEGMNTD_FILE_PATHS);

			break;
		case MosipBioDeviceConstants.VALUE_FINGERPRINT + "_" + MosipBioDeviceConstants.VALUE_SLAP_THUMB:
			mosipBioCaptureResponseDto.setSlapImage(getCapturedByte(bioType));
			stubSegmentedBiometrics(mosipBioRequest, mosipBioCaptureResponses,
					MosipBioDeviceConstants.THUMBS_SEGMNTD_FILE_PATHS);

			break;
		case MosipBioDeviceConstants.VALUE_FINGERPRINT + "_" + MosipBioDeviceConstants.VALUE_SLAP_LEFT_ONBOARD:

			mosipBioCaptureResponseDto.setSlapImage(getCapturedByte(
					MosipBioDeviceConstants.VALUE_FINGERPRINT + "_" + MosipBioDeviceConstants.VALUE_SLAP_LEFT));
			stubSegmentedBiometrics(mosipBioRequest, mosipBioCaptureResponses,
					MosipBioDeviceConstants.LEFTHAND_SEGMNTD_FILE_PATHS_USERONBOARD);

			break;
		case MosipBioDeviceConstants.VALUE_FINGERPRINT + "_" + MosipBioDeviceConstants.VALUE_SLAP_RIGHT_ONBOARD:
			mosipBioCaptureResponseDto.setSlapImage(getCapturedByte(
					MosipBioDeviceConstants.VALUE_FINGERPRINT + "_" + MosipBioDeviceConstants.VALUE_SLAP_RIGHT));
			stubSegmentedBiometrics(mosipBioRequest, mosipBioCaptureResponses,
					MosipBioDeviceConstants.RIGHTHAND_SEGMNTD_FILE_PATHS_USERONBOARD);

			break;
		case MosipBioDeviceConstants.VALUE_FINGERPRINT + "_" + MosipBioDeviceConstants.VALUE_SLAP_THUMB_ONBOARD:
			mosipBioCaptureResponseDto.setSlapImage(getCapturedByte(
					MosipBioDeviceConstants.VALUE_FINGERPRINT + "_" + MosipBioDeviceConstants.VALUE_SLAP_THUMB));
			stubSegmentedBiometrics(mosipBioRequest, mosipBioCaptureResponses,
					MosipBioDeviceConstants.THUMBS_SEGMNTD_FILE_PATHS_USERONBOARD);

			break;
		case MosipBioDeviceConstants.VALUE_FINGERPRINT + "_" + MosipBioDeviceConstants.VALUE_TOUCHLESS:
			stubImage(mosipBioCaptureResponses, mosipBioRequest, bioType);
			break;
		case MosipBioDeviceConstants.VALUE_FACE:
			stubImage(mosipBioCaptureResponses, mosipBioRequest, bioType);
			break;
		case MosipBioDeviceConstants.VALUE_IRIS + "_" + MosipBioDeviceConstants.VALUE_SINGLE:
			stubImage(mosipBioCaptureResponses, mosipBioRequest, bioType);
			break;
		case MosipBioDeviceConstants.VALUE_IRIS + "_" + MosipBioDeviceConstants.VALUE_DOUBLE:
			stubImage(mosipBioCaptureResponses, mosipBioRequest, bioType);
			break;
		case MosipBioDeviceConstants.VALUE_VEIN:
			stubImage(mosipBioCaptureResponses, mosipBioRequest, bioType);
			break;
		default:
			break;
		}

		return mosipBioCaptureResponseDto;
	}

	protected void stubSegmentedBiometrics(MosipBioRequest mosipBioRequest,
			List<MosipBioCaptureResponse> mosipBioCaptureResponses, String[] biometricPaths) {
		List<String> filePaths = Arrays.asList(biometricPaths);

		for (String folderPath : filePaths) {
			String[] imageFileName = folderPath.split("/");
			MosipBioCaptureResponse mosipBioCaptureResponseSegmented = new MosipBioCaptureResponse();
			CaptureResponseData captureResponseSegmentedData = new CaptureResponseData();
			byte[] isoTemplateBytes = null;
			byte[] isoImageBytes = null;
			try {
				isoTemplateBytes = IOUtils.resourceToByteArray(folderPath.concat(MosipBioDeviceConstants.ISO_FILE));

				/* Base64 encoded data */
				captureResponseSegmentedData.setBioExtract(Base64.getEncoder().encode(isoTemplateBytes));

				isoImageBytes = IOUtils.resourceToByteArray(folderPath.concat(MosipBioDeviceConstants.ISO_IMAGE_FILE));

				/* Basse64 encoded data */
				captureResponseSegmentedData.setBioValue(Base64.getEncoder().encode(isoImageBytes));

			} catch (IOException e) {
				e.printStackTrace();
			}

			/*imageFileName[3] = imageFileName[3].replace("left", "Left ").replace("right", "Right ").replace("thumb", "Thumb ");*/
			captureResponseSegmentedData.setBioSubType(imageFileName[3]);
			captureResponseSegmentedData.setBioType(mosipBioRequest.getDeviceId());
			captureResponseSegmentedData.setDeviceCode("");
			captureResponseSegmentedData.setDeviceProviderID("");
			captureResponseSegmentedData.setDeviceServiceVersion("");
			captureResponseSegmentedData.setQualityScore("90");
			captureResponseSegmentedData.setRequestedScore(mosipBioRequest.getRequestedScore());
			captureResponseSegmentedData.setTimestamp(new Timestamp(System.currentTimeMillis()) + "");
			captureResponseSegmentedData.setTransactionID("");
			captureResponseSegmentedData.setBioSegmentedType(imageFileName[3]);

			setEncodedCapturedResponse(mosipBioCaptureResponses, mosipBioCaptureResponseSegmented,
					captureResponseSegmentedData);

		}
	}

	/**
	 * Stub the image for the response
	 * 
	 * @param CaptureResponseData
	 * @param MosipBioRequest
	 * @return
	 */
	private void stubImage(List<MosipBioCaptureResponse> mosipBioCaptureResponses, MosipBioRequest mosipBioRequest,
			String bioType) {

		MosipBioCaptureResponse mosipBioCaptureResponse = new MosipBioCaptureResponse();
		CaptureResponseData captureResponseData = new CaptureResponseData();

		captureResponseData.setBioSubType(mosipBioRequest.getDeviceSubId());
		captureResponseData.setBioType(mosipBioRequest.getDeviceId());
		captureResponseData.setBioValue(getCapturedByte(bioType));
		captureResponseData.setBioExtract(getCapturedByte(bioType));
		captureResponseData.setDeviceCode("");
		captureResponseData.setDeviceProviderID("");
		captureResponseData.setDeviceServiceVersion("");
		captureResponseData.setQualityScore("90");
		captureResponseData.setRequestedScore(mosipBioRequest.getRequestedScore());
		captureResponseData.setTimestamp(new Timestamp(System.currentTimeMillis()) + "");
		captureResponseData.setTransactionID("");

		setEncodedCapturedResponse(mosipBioCaptureResponses, mosipBioCaptureResponse, captureResponseData);
	}

	protected void setEncodedCapturedResponse(List<MosipBioCaptureResponse> mosipBioCaptureResponses,
			MosipBioCaptureResponse mosipBioCaptureResponse, CaptureResponseData captureResponseData) {
		mosipBioCaptureResponse.setCaptureBioData(getBase64EncodedJson(captureResponseData));
		mosipBioCaptureResponse.setHash("sha256");
		mosipBioCaptureResponse.setSessionKey("123");
		mosipBioCaptureResponse.setSignature("base 64 data sample");
		mosipBioCaptureResponses.add(mosipBioCaptureResponse);
	}

	protected String getBase64EncodedJson(CaptureResponseData captureResponseData) {
		try {
			return Base64.getEncoder()
					.encodeToString(new ObjectMapper().writeValueAsString(captureResponseData).getBytes());
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Get the captured byte
	 * 
	 * @param String
	 *            -imageType
	 * @return byte[]
	 */
	private byte[] getCapturedByte(String imageType) {
		String imgFormat = ".png";
		if (imageType.equals("FINGERPRINT_SINGLE")) {
			imgFormat = ".iso";
			imageType = "ISOTemplate";
		}
		byte[] scannedBytes = null;
		try {
			scannedBytes = Base64.getEncoder().encode(
					IOUtils.toByteArray(this.getClass().getResourceAsStream("/images/" + imageType + imgFormat)));
		} catch (IOException exceptoin) {
			exceptoin.printStackTrace();
		}

		return scannedBytes;

	}

}
