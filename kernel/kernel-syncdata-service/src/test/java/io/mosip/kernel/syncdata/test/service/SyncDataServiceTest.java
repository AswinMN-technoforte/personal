package io.mosip.kernel.syncdata.test.service;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withBadRequest;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.env.Environment;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import io.mosip.kernel.syncdata.dto.ApplicationDto;
import io.mosip.kernel.syncdata.dto.HolidayDto;
import io.mosip.kernel.syncdata.dto.MachineDto;
import io.mosip.kernel.syncdata.dto.MachineSpecificationDto;
import io.mosip.kernel.syncdata.dto.MachineTypeDto;
import io.mosip.kernel.syncdata.dto.response.MasterDataResponseDto;
import io.mosip.kernel.syncdata.exception.SyncDataServiceException;
import io.mosip.kernel.syncdata.service.SyncConfigDetailsService;
import io.mosip.kernel.syncdata.service.SyncMasterDataService;
import io.mosip.kernel.syncdata.utils.SyncMasterDataServiceHelper;
import net.minidev.json.JSONObject;

@SpringBootTest
@RunWith(SpringRunner.class)
public class SyncDataServiceTest {
	@MockBean
	private SyncMasterDataServiceHelper masterDataServiceHelper;

	@Autowired
	private SyncMasterDataService masterDataService;

	@Autowired
	RestTemplate restemplate;

	/**
	 * Environment instance
	 */
	@Autowired
	private Environment env;

	/**
	 * file name referred from the properties file
	 */
	@Value("${mosip.kernel.syncdata.registration-center-config-file}")
	private String regCenterfileName;

	/**
	 * file name referred from the properties file
	 */
	@Value("${mosip.kernel.syncdata.global-config-file}")
	private String globalConfigFileName;

	private String configServerUri = null;
	private String configLabel = null;
	private String configProfile = null;
	private String configAppName = null;

	private StringBuilder uriBuilder;

	@Autowired
	private SyncConfigDetailsService syncConfigDetailsService;
	private MasterDataResponseDto masterDataResponseDto;
	private List<ApplicationDto> applications;
	List<HolidayDto> holidays;
	List<MachineDto> machines;
	List<MachineSpecificationDto> machineSpecifications;
	List<MachineTypeDto> machineTypes;

	JSONObject globalConfigMap = null;
	JSONObject regCentreConfigMap = null;

	@Before
	public void setup() {
		masterDataSyncSetup();
		configDetialsSyncSetup();
	}

	public void masterDataSyncSetup() {
		masterDataResponseDto = new MasterDataResponseDto();
		applications = new ArrayList<>();
		applications.add(new ApplicationDto("01", "REG FORM", "REG Form", "ENG", true));
		masterDataResponseDto.setApplications(applications);
		holidays = new ArrayList<>();
		holidays.add(new HolidayDto("1", "2018-01-01", "01", "01", "2018", "NEW YEAR", "ENG", "LOC01", true));
		masterDataResponseDto.setHolidays(holidays);
		machines = new ArrayList<>();
		machines.add(new MachineDto("1001", "Laptop", "QWE23456", "1223:23:31:23", "172.12.128.1", "1", "ENG", true,
				LocalDateTime.parse("2018-01-01T01:01:01")));
		masterDataResponseDto.setMachineDetails(machines);
		machineSpecifications = new ArrayList<>();
		machineSpecifications.add(new MachineSpecificationDto("1", "lenovo Thinkpad", "Lenovo", "T480", "1", "1.0.1",
				"Thinkpad", "ENG", true));
		masterDataResponseDto.setMachineSpecification(machineSpecifications);
		machineTypes = new ArrayList<>();
		machineTypes.add(new MachineTypeDto("1", "ENG", "Laptop", "Laptop", true));
		masterDataResponseDto.setMachineType(machineTypes);
	}

	public void configDetialsSyncSetup() {
		globalConfigMap = new JSONObject();
		globalConfigMap.put("archivalPolicy", "arc_policy_2");
		globalConfigMap.put("otpTimeOutInMinutes", 2);
		globalConfigMap.put("numberOfWrongAttemptsForOtp", 5);
		globalConfigMap.put("uinLength", 24);

		regCentreConfigMap = new JSONObject();

		regCentreConfigMap.put("fingerprintQualityThreshold", 120);
		regCentreConfigMap.put("irisQualityThreshold", 25);
		regCentreConfigMap.put("irisRetryAttempts", 10);
		regCentreConfigMap.put("faceQualityThreshold", 25);
		regCentreConfigMap.put("faceRetry", 12);
		regCentreConfigMap.put("supervisorVerificationRequiredForExceptions", true);
		regCentreConfigMap.put("operatorRegSubmissionMode", "fingerprint");
		configServerUri = env.getProperty("spring.cloud.config.uri");
		configLabel = env.getProperty("spring.cloud.config.label");
		configProfile = env.getProperty("spring.profiles.active");
		configAppName = env.getProperty("spring.application.name");
		uriBuilder = new StringBuilder();
		uriBuilder.append(configServerUri + "/").append(configAppName + "/").append(configProfile + "/")
				.append(configLabel + "/");

	}

	@Test(expected = SyncDataServiceException.class)
	public void syncDataFailure() throws InterruptedException, ExecutionException {
		when(masterDataServiceHelper.getMachines(Mockito.anyString(), Mockito.any()))
				.thenThrow(SyncDataServiceException.class);
		masterDataService.syncData("1001", null);

	}

	@Test
	public void globalConfigsyncSuccess() {

		MockRestServiceServer server = MockRestServiceServer.bindTo(restemplate).build();
		server.expect(requestTo(uriBuilder.append(globalConfigFileName).toString())).andRespond(withSuccess());
		syncConfigDetailsService.getGlobalConfigDetails();

	}

	@Test
	public void registrationConfigsyncSuccess() {
		MockRestServiceServer server = MockRestServiceServer.bindTo(restemplate).build();
		server.expect(requestTo(uriBuilder.append(regCenterfileName).toString())).andRespond(withSuccess());
		syncConfigDetailsService.getRegistrationCenterConfigDetails("1");
		// Assert.assertEquals(120, jsonObject.get("fingerprintQualityThreshold"));
	}

	@Test(expected = SyncDataServiceException.class)
	public void registrationConfigsyncFailure() {

		MockRestServiceServer server = MockRestServiceServer.bindTo(restemplate).build();
		server.expect(requestTo(uriBuilder.append(regCenterfileName).toString())).andRespond(withBadRequest());
		syncConfigDetailsService.getRegistrationCenterConfigDetails("1");
	}

	@Test(expected = SyncDataServiceException.class)
	public void globalConfigsyncFailure() {

		MockRestServiceServer server = MockRestServiceServer.bindTo(restemplate).build();
		server.expect(requestTo(uriBuilder.append(globalConfigFileName).toString())).andRespond(withBadRequest());
		syncConfigDetailsService.getGlobalConfigDetails();
	}

	@Test(expected = SyncDataServiceException.class)
	public void globalConfigsyncFileNameNullFailure() {

		MockRestServiceServer server = MockRestServiceServer.bindTo(restemplate).build();
		server.expect(requestTo(uriBuilder.append(globalConfigFileName).toString())).andRespond(withBadRequest());
		syncConfigDetailsService.getGlobalConfigDetails();
	}

	//@Test
	public void getConfigurationSuccess() {
		MockRestServiceServer server = MockRestServiceServer.bindTo(restemplate).build();
		server.expect(requestTo(uriBuilder.append(globalConfigFileName).toString())).andRespond(withSuccess());
		uriBuilder = new StringBuilder();
		uriBuilder.append(configServerUri + "/").append(configAppName + "/").append(configProfile + "/")
				.append(configLabel + "/");
		server.expect(requestTo(uriBuilder.append(regCenterfileName).toString())).andRespond(withSuccess());
		syncConfigDetailsService.getConfiguration("1");
	}
}
