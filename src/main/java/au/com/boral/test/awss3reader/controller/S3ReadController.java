package au.com.boral.test.awss3reader.controller;

import au.com.boral.test.awss3reader.services.S3Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Rest controller to provide API to access objects in Amazon S3 bucket
 * The controller defines url paths and delegates S3 job to S3Service class
 */
@RestController
@EnableAutoConfiguration
@RequestMapping("/s3api")
@ComponentScan("au.com.boral.test.awss3reader.services")
public class S3ReadController {

	@Autowired
	private S3Service client;

	@RequestMapping("/objects")
	public List listObjects() {
		return client.getObjects(false);
	}

	@RequestMapping("/keys")
	public List listKeys() {
		return client.getObjects(true);
	}

	@RequestMapping("/objects/{key:.+}")
	public Map<String, Object> getObject(@PathVariable String key) {
		return client.getObject(key);
	}

	@RequestMapping("/allobjects")
	public List allObjectDetails() {
		return ((List<String>) client.getObjects(true)).stream()
				.map(client::getObject)
				.collect(Collectors.toList());
	}

	public static void main(String[] args) {
		SpringApplication.run(S3ReadController.class, args);
	}
}