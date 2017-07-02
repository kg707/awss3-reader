package au.com.boral.test.awss3reader.services;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * JUnit test for S3Service class
 * Mocks S3 API connection and supplies pre-fabricated objects as S3 response
 */
public class S3ServiceTest {

	private static final int BATCH_SIZE = 3;
	private static final int BATCH_COUNT = 5;
	private static final String TEST_BODY = "Hello world";

	private S3Service service = spy(S3Service.class);

	@Mock
	private AmazonS3 amazonS3;

	@Mock
	private ObjectListing objectListing;

	@Mock
	private S3Object s3Object;

	@Mock
	private ObjectMetadata metadata;

	@Before
	public void init() {
		MockitoAnnotations.initMocks(this);
		doReturn(amazonS3).when(service).getAmazonS3Client();
		when(amazonS3.listObjects(any(String.class))).thenReturn(objectListing);
		when(amazonS3.listNextBatchOfObjects(objectListing)).thenReturn(objectListing);
	}

	@Test
	public void getObjectsEmptyList() {
		List objects = service.getObjects(false);
		assertEquals(0, objects.size());
	}

	@Test
	public void getObjectsFails() {
		when(amazonS3.listObjects(any(String.class))).thenThrow(AmazonClientException.class);

		List objects = service.getObjects(false);

		assertEquals(1, objects.size());
		assertEquals("A system error has occurred", objects.get(0));
	}

	@Test
	public void getObjectsWithValues() {
		when(objectListing.getObjectSummaries()).thenReturn(testList(BATCH_SIZE));

		List objects = service.getObjects(false);

		assertEquals(BATCH_SIZE, objects.size());
		assertEquals("TestKey0", ((S3ObjectSummary) objects.get(0)).getKey());
	}

	@Test
	public void getObjectsWithCycle() {
		// Can't use thenReturn, as it returns the same object reference every time
		when(objectListing.getObjectSummaries()).thenAnswer(answer -> testList(BATCH_SIZE));
		final int[] isTruncatedCallCount = new int[1];
		// Break the cycle after BATCH_COUNT runs
		when(objectListing.isTruncated()).thenAnswer(answer -> isTruncatedCallCount[0]++ < BATCH_COUNT);

		List objects = service.getObjects(false);

		int expectedSize = BATCH_SIZE * (BATCH_COUNT + 1);
		assertEquals(expectedSize, objects.size());
		assertEquals("TestKey" + (BATCH_SIZE - 1), ((S3ObjectSummary) objects.get(expectedSize - 1)).getKey());
	}

	@Test
	public void getKeysWithCycle() {
		// Can't use thenReturn, as it returns the same object reference every time
		when(objectListing.getObjectSummaries()).thenAnswer(answer -> testList(BATCH_SIZE));
		final int[] isTruncatedCallCount = new int[1];
		// Break the cycle after BATCH_COUNT runs
		when(objectListing.isTruncated()).thenAnswer(answer -> isTruncatedCallCount[0]++ < BATCH_COUNT);

		List objects = service.getObjects(true);

		int expectedSize = BATCH_SIZE * (BATCH_COUNT + 1);
		assertEquals(expectedSize, objects.size());
		assertEquals("TestKey" + (BATCH_SIZE - 1), objects.get(expectedSize - 1));
	}

	@Test
	public void getObjectByKey() {
		when(amazonS3.getObject(any(GetObjectRequest.class))).thenReturn(s3Object);
		when(s3Object.getObjectContent()).thenReturn(new S3ObjectInputStream(new ByteArrayInputStream(TEST_BODY.getBytes()), null));
		when(s3Object.getObjectMetadata()).thenReturn(metadata);

		Map<String, Object> object = service.getObject("test1.txt");

		assertEquals(TEST_BODY, object.get("body"));
	}

	@Test
	public void getObjectByKeyFails() {
		when(amazonS3.getObject(any(GetObjectRequest.class))).thenThrow(AmazonServiceException.class);

		Map<String, Object> object = service.getObject("test1.txt");

		assertEquals("A system error has occurred", object.get("error"));
	}

	// Creates a dummy list of S3 objects
	private List<S3ObjectSummary> testList(int size) {
		List<S3ObjectSummary> list = new ArrayList<>();
		for (int i = 0; i < size; i++) {
			S3ObjectSummary objectSummary = new S3ObjectSummary();
			objectSummary.setKey("TestKey" + i);
			list.add(objectSummary);
		}
		return list;
	}
}