package io.mosip.kernel.cryptomanager.test;

import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.time.LocalDateTime;

import javax.crypto.SecretKey;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.mosip.kernel.core.crypto.spi.Decryptor;
import io.mosip.kernel.core.exception.NoSuchAlgorithmException;
import io.mosip.kernel.core.util.CryptoUtil;
import io.mosip.kernel.cryptomanager.KernelCryptomanagerBootApplication;
import io.mosip.kernel.cryptomanager.dto.CryptomanagerRequestDto;
import io.mosip.kernel.cryptomanager.dto.KeymanagerPublicKeyResponseDto;
import io.mosip.kernel.cryptomanager.utils.CryptomanagerUtil;

@SpringBootTest(classes = KernelCryptomanagerBootApplication.class)
@RunWith(SpringRunner.class)
@AutoConfigureMockMvc
@DirtiesContext(classMode=ClassMode.AFTER_EACH_TEST_METHOD)
public class KernelCryptographicUtilExceptionTest {

	@Autowired
	CryptomanagerUtil cryptomanagerUtil;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private RestTemplate restTemplate;

	@MockBean
	Decryptor<PrivateKey, PublicKey, SecretKey> decryptor;

	private MockRestServiceServer server;

	@Before
	public void setUp() {
		server = MockRestServiceServer.bindTo(restTemplate).build();
		ReflectionTestUtils.setField(cryptomanagerUtil, "asymmetricAlgorithmName", "test");
	}

	@Test(expected=NoSuchAlgorithmException.class)
	public void testNoSuchAlgorithmEncrypt() throws Exception {
		KeymanagerPublicKeyResponseDto keymanagerPublicKeyResponseDto = new KeymanagerPublicKeyResponseDto(
				CryptoUtil.encodeBase64("badprivatekey".getBytes()), LocalDateTime.now(),
				LocalDateTime.now().plusDays(100));
		server.expect(requestTo(
				"http://localhost:8088/keymanager/v1.0/publickey/REGISTRATION?timeStamp=2018-12-06T12:07:44.403&referenceId=ref123"))
				.andRespond(withSuccess(objectMapper.writeValueAsString(keymanagerPublicKeyResponseDto),
						MediaType.APPLICATION_JSON));
		CryptomanagerRequestDto cryptomanagerRequestDto= new CryptomanagerRequestDto("REGISTRATION","ref123",LocalDateTime.parse("2018-12-06T12:07:44.403"),"test");
	cryptomanagerUtil.getPublicKey(cryptomanagerRequestDto);
	}
}
