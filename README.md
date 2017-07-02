# awss3-reader
A small project to read from S3 bucket and return objects via REST service

#### Project files
There are two application classes, two unit tests, two configuration files and pom.xml

###### Java classes
S3ReadController - defines rest API urls and delegates work to service class

S3Service - Interacts with Amazon S3 and returns a file or list of objects

###### Configuration files
application.properties - contains Amazon S3 connection details and bucket name

log4j.xml - defines logging settings for the application

###### Unit tests
S3ServiceTest - Unit test, mocks S3 responses

S3ServiceIntegrationTest - Integration test, talks to real S3 bucket

###### Build and other files
pom.xml - defines libraries used (Spring Boot, Amazon S3, logging, unit tests and mocks), and compilation, test and running strategies

.gitignore - exclusion file list for source control

README.md - notes about the project

#### Running the project
The project needs Java 8 and Maven 3 to run

Use `mvn clean install` to build and run unit tests

To start the service, enter `mvn spring-boot:run`
The service starts at [http://localhost:8080/s3api/objects](http://localhost:8080/s3api/objects)

If your local default port is already in use, then pass new port number as parameter: `mvn spring-boot:run -Dserver.port=8000`

Running integration tests: `mvn surefire:test -Dtest=*IntegrationTest`

Running sonar report: `mvn sonar:sonar`

The logs are stored and rotated in `/apps/logs` folder or `c:\apps\logs\`

#### Using the service

Default configuration starts the service in `/s3api` context

List of metadata for each object in the bucket: [http://localhost:8080/s3api/objects/](http://localhost:8080/s3api/objects/)

List of all object keys: [http://localhost:8080/s3api/keys/](http://localhost:8080/s3api/keys/)

An object attributes, including content in the `body` attribute, can be retrieved by its key: `http://localhost:8080/s3api/objects/{key}`, for example [http://localhost:8080/s3api/objects/test.log](http://localhost:8080/s3api/objects/test.log)

All object details in one go are returned here: [http://localhost:8080/s3api/allobjects](http://localhost:8080/s3api/allobjects)


###### TODOs
Improve different content type handling