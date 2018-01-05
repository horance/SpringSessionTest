package org.gradle;

import java.util.Properties;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.redis.connection.RedisClusterConnection;
import org.springframework.data.redis.connection.RedisClusterNode;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@EnableRedisHttpSession
@RestController
public class TestApplication {

	public static void main(String[] args) {
		SpringApplication.run(TestApplication.class, args);
	}

	@Autowired
	private RedisTemplate redisTemplate;

	@RequestMapping(path = "/hello", produces = MediaType.TEXT_PLAIN_VALUE, method = RequestMethod.GET)
	public String hello(@PathVariable(required = false) String name, HttpServletRequest request) {
		HttpSession session = request.getSession(true);
		session.setMaxInactiveInterval(60);
		String sessionId = session.getId();
		String springSessionExpiresKey = "spring:session:sessions:expires:" + sessionId;
		RedisClusterConnection clusterConnection = redisTemplate.getConnectionFactory().getClusterConnection();
		RedisClusterNode node = clusterConnection.clusterGetNodeForKey(springSessionExpiresKey.getBytes());
		Properties info = clusterConnection.info();
		String patternCount = info.getProperty(node.asString() + ".pubsub_patterns");
		boolean notExpired = "0".equals(patternCount);
		return "The session ID is :" + sessionId + ", session expires key stored on node: " + node.asString() +
				", psub pattern count is " + patternCount + ", will " + (notExpired ? "not " : "")
				+ "receive expired event.";
	}

}
