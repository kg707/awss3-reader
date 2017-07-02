package au.com.boral.test.awss3reader.services;

import au.com.boral.test.awss3reader.controller.S3ReadController;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;

/**
 * Integration test
 * Loads list of keys and an object from S3
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {S3ReadController.class}, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class S3ServiceIntegrationTest {

	private static final int CURRENT_BUCKET_SIZE = 3;

	@Autowired
	private S3Service service;

	@Test
	public void testGetObjects() throws Exception {
		List list = service.getObjects(false);
		assertEquals(CURRENT_BUCKET_SIZE, list.size());
	}

	@Test
	public void testGetObject() throws Exception {
		Map<String, Object> object = service.getObject("test.log");
		assertEquals("Hello world", object.get("body"));
	}
}