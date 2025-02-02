package io.mosip.kernel.masterdata.dto.postresponse;

import java.util.List;

import io.mosip.kernel.masterdata.dto.getresponse.extn.RegistrationCenterExtnDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RegistrationCenterPostResponseDto {
	private List<RegistrationCenterExtnDto> registrationCenters ;
}
