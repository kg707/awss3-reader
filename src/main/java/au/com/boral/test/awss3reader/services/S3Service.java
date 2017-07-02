package au.com.boral.test.awss3reader.services;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.amazonaws.util.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * A Service layer component. Talks to Amazon S3.
 * Reads objects from bucket and reads object body by key.
 * Uses application.properties to configure Amazon S3 connection
 */
@Service
@Configuration
@PropertySource("classpath:application.properties")
public class S3Service {

	private static final Logger LOGGER = LoggerFactory.getLogger(S3Service.class);
	// Default error message returned to client
	private static final String ERROR_GENERIC = "A system error has occurred";
	private static final String ERROR_NOT_FOUND = "Object not found";

	private static final String ATTR_ERROR = "error";
	private static final String ATTR_BODY = "body";

	@Value("${amazon.s3.bucket}")
	private String bucket;
	@Value("${amazon.s3.endpoint}")
	private String endpoint;
	@Value("${amazon.s3.region}")
	private String region;

	/**
	 * Retrieves list all objects or object keys in the bucket
	 * @param keysOnly
	 * @return list of objects or keys
	 */
	public List getObjects(boolean keysOnly) {
		LOGGER.debug("Loading list of objects from S3, keysOnly=[{}]", keysOnly);
		try {
			AmazonS3 s3client = getAmazonS3Client();
			ObjectListing listing = s3client.listObjects(bucket);
			List objects = truncate(listing.getObjectSummaries(), keysOnly);

			while (listing.isTruncated()) {
				listing = s3client.listNextBatchOfObjects(listing);
				objects.addAll(truncate(listing.getObjectSummaries(), keysOnly));
			}
			return objects;
		} catch (AmazonServiceException ase) {
			LOGGER.error("Service issue retrieving list of S3 objects", ase);
		} catch (AmazonClientException ace) {
			LOGGER.error("Client issue retrieving list of S3 objects", ace);
		}

		return Collections.singletonList(ERROR_GENERIC);
	}

	/**
	 * Retrieves an object from S3 bucket by the key supplied
	 * @param key
	 * @return S3 object
	 */
	public Map<String, Object> getObject(String key) {
		LOGGER.debug("Loading object from S3 by key [{}]", key);
		Map<String, Object> fullObject = new HashMap<>();
		try {
			AmazonS3 s3client = getAmazonS3Client();
			S3Object s3object = s3client.getObject(new GetObjectRequest(bucket, key));
			S3ObjectInputStream stream = s3object.getObjectContent();

			fullObject.putAll(s3object.getObjectMetadata().getRawMetadata());
			fullObject.put(ATTR_BODY, IOUtils.toString(stream));
		} catch (AmazonServiceException ase) {
			if (ase.getStatusCode() == 404) {
				fullObject.put(ATTR_ERROR, ERROR_NOT_FOUND);
			} else {
				fullObject.put(ATTR_ERROR, ERROR_GENERIC);
				LOGGER.error("Service issue retrieving S3 object for key [{}]", key, ase);
			}
		} catch (AmazonClientException ace) {
			fullObject.put(ATTR_ERROR, ERROR_GENERIC);
			LOGGER.error("Client issue retrieving S3 object for key [{}]", key, ace);
		} catch (IOException ioe) {
			fullObject.put(ATTR_ERROR, ERROR_GENERIC);
			LOGGER.error("Input stream reading failed for key [{}]", key, ioe);
		}
		LOGGER.info("Returning obj [{}]", fullObject);
		return fullObject;
	}

	/**
	 * Creates an Amazon S3 client from endpoint and region
	 *
	 * @return an Amazon S3 client
	 */
	protected AmazonS3 getAmazonS3Client() {
		LOGGER.debug("getAmazonS3Client start");
		// Create a new S3 client using the configured endpoint and region
		return AmazonS3Client.builder().withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(endpoint, region)).build();
	}

	/**
	 * The method converts list of objects into list of keys, if needed
	 * @param objectSummaries list of S3 objects
	 * @param keysOnly flag to strip object data or not
	 * @return list of objects or list of object keys
	 */
	private List truncate(List<S3ObjectSummary> objectSummaries, boolean keysOnly) {
		if (keysOnly) {
			// Build a list of keys only
			return objectSummaries.stream()
					.map(S3ObjectSummary::getKey)
					.collect(Collectors.toList());
		} else {
			return objectSummaries;
		}
	}
}