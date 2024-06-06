package study.microservices.core.product;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan("study")
public class ProductServiceApplication {
	private static final Logger LOG = LoggerFactory.getLogger(ProductServiceApplication.class);

	public static void main(String[] args) {
		// Start up the microservice and retrieve the context
		ConfigurableApplicationContext ctx = SpringApplication.run(ProductServiceApplication.class, args);

		// Print out the database connection of the microservice
		String dbHost = ctx.getEnvironment().getProperty("spring.data.mongodb.host");
		String dbPort = ctx.getEnvironment().getProperty("spring.data.mongodb.port");
		LOG.info("Connected to MongoDb: " + dbHost + ":" + dbPort);
	}

}
