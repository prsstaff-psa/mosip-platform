package io.mosip.registration.test.service.packet.encryption;

import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import io.mosip.registration.audit.AuditManagerSerivceImpl;
import io.mosip.registration.constants.AuditEvent;
import io.mosip.registration.constants.Components;
import io.mosip.registration.constants.RegistrationConstants;
import io.mosip.registration.context.ApplicationContext;
import io.mosip.registration.context.SessionContext;
import io.mosip.registration.dao.AuditLogControlDAO;
import io.mosip.registration.dao.RegistrationDAO;
import io.mosip.registration.dto.RegistrationDTO;
import io.mosip.registration.dto.ResponseDTO;
import io.mosip.registration.entity.AuditLogControl;
import io.mosip.registration.exception.RegBaseCheckedException;
import io.mosip.registration.exception.RegBaseUncheckedException;
import io.mosip.registration.service.external.StorageService;
import io.mosip.registration.service.packet.impl.PacketEncryptionServiceImpl;
import io.mosip.registration.service.security.AESEncryptionService;
import io.mosip.registration.test.util.datastub.DataProvider;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ ApplicationContext.class, SessionContext.class })
public class PacketEncryptionServiceTest {

	@Rule
	public MockitoRule mockitoRule = MockitoJUnit.rule();
	@InjectMocks
	private PacketEncryptionServiceImpl packetEncryptionServiceImpl;
	@Mock
	private AESEncryptionService aesEncryptionService;
	@Mock
	private StorageService storageService;
	@Mock
	private RegistrationDAO registrationDAO;
	@Mock
	private AuditManagerSerivceImpl auditFactory;
	@Mock
	private AuditLogControlDAO auditLogControlDAO;
	private RegistrationDTO registrationDTO;

	@Before
	public void initialize() throws Exception {
		
		registrationDTO = DataProvider.getPacketDTO();
		Map<String,Object> appMap = new HashMap<>();
		appMap.put(RegistrationConstants.REG_PKT_SIZE, "1");

		doNothing().when(auditFactory).audit(Mockito.any(AuditEvent.class), Mockito.any(Components.class),
				Mockito.anyString(), Mockito.anyString());
		doNothing().when(auditLogControlDAO).save(Mockito.any(AuditLogControl.class));
		PowerMockito.mockStatic(ApplicationContext.class, SessionContext.class);
		PowerMockito.doReturn(appMap).when(ApplicationContext.class, "map");
		PowerMockito.doReturn(Mockito.mock(SessionContext.UserContext.class)).when(SessionContext.class, "userContext");
		PowerMockito.when(SessionContext.userContext().getUserId()).thenReturn("mosip");
	}

	@Test
	public void testEncryption() throws RegBaseCheckedException {
		when(aesEncryptionService.encrypt(Mockito.anyString().getBytes())).thenReturn("Encrypted_Data".getBytes());
		when(storageService.storeToDisk(Mockito.anyString(), Mockito.anyString().getBytes())).thenReturn("D:/Packet Store/27-Sep-2018/1111_Ack.jpg");
		doNothing().when(registrationDAO).save(Mockito.anyString(), Mockito.any(RegistrationDTO.class));
		when(auditLogControlDAO.getLatestRegistrationAuditDates()).thenReturn(null);

		ResponseDTO responseDTO = packetEncryptionServiceImpl.encrypt(registrationDTO, "PacketZip".getBytes());

		Assert.assertEquals("0000", responseDTO.getSuccessResponseDTO().getCode());
		Assert.assertEquals("Success", responseDTO.getSuccessResponseDTO().getMessage());
	}
	
	@SuppressWarnings("unchecked")
	@Test(expected = RegBaseCheckedException.class)
	public void testCheckedException() throws RegBaseCheckedException {
		when(aesEncryptionService.encrypt(Mockito.anyString().getBytes())).thenThrow(RegBaseCheckedException.class);
		when(storageService.storeToDisk(Mockito.anyString(), Mockito.anyString().getBytes())).thenReturn("D:/Packet Store/27-Sep-2018/1111_Ack.jpg");
		doNothing().when(registrationDAO).save(Mockito.anyString(), Mockito.any(RegistrationDTO.class));

		packetEncryptionServiceImpl.encrypt(registrationDTO, "PacketZip".getBytes());
	}
	
	@SuppressWarnings("unchecked")
	@Test(expected = RegBaseUncheckedException.class)
	public void testUncheckedException() throws RegBaseCheckedException {
		when(aesEncryptionService.encrypt(Mockito.anyString().getBytes())).thenThrow(RuntimeException.class);
		when(storageService.storeToDisk(Mockito.anyString(), Mockito.anyString().getBytes())).thenReturn("D:/Packet Store/27-Sep-2018/1111_Ack.jpg");
		doNothing().when(registrationDAO).save(Mockito.anyString(), Mockito.any(RegistrationDTO.class));

		packetEncryptionServiceImpl.encrypt(new RegistrationDTO(), "PacketZip".getBytes());
	}
	
	@Test
	public void packetSizeExceededTest() throws RegBaseCheckedException {
		byte[] encryptedData = new byte[1050576];
		when(aesEncryptionService.encrypt(Mockito.anyString().getBytes())).thenReturn(encryptedData);
		when(storageService.storeToDisk(Mockito.anyString(), Mockito.anyString().getBytes())).thenReturn("D:/Packet Store/27-Sep-2018/1111_Ack.jpg");
		doNothing().when(registrationDAO).save(Mockito.anyString(), Mockito.any(RegistrationDTO.class));

		packetEncryptionServiceImpl.encrypt(registrationDTO, "PacketZip".getBytes());
	}

}
