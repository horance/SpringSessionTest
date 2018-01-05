package org.gradle;

import java.util.Arrays;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.http.MediaType;
import org.springframework.session.data.redis.RedisOperationsSessionRepository;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;
import org.springframework.util.StringUtils;
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

	@RequestMapping(path = "/hello/{name}", produces = MediaType.TEXT_PLAIN_VALUE, method = RequestMethod.GET)
	public String hello(@PathVariable(required = false) String name, HttpServletRequest request) {
		HttpSession session = request.getSession(true);
		session.setMaxInactiveInterval(30);
		return "Hello, " + (StringUtils.isEmpty(name) ? " there " : name) + ". " + session.getId();
	}
	
	@Bean
	public RedisMessageListenerContainer redisMessageListenerContainer(
			RedisConnectionFactory connectionFactory,
			RedisOperationsSessionRepository messageListener) {
		System.out.println("running in customized config");
		RedisMessageListenerContainer container = new RedisMessageListenerContainer();
		container.setConnectionFactory(connectionFactory);
		container.addMessageListener(messageListener,
				Arrays.asList(new PatternTopic("__keyevent@*:del"),
						new PatternTopic("__keyevent@*:expired")));
		container.addMessageListener(messageListener, Arrays.asList(new PatternTopic(
				messageListener.getSessionCreatedChannelPrefix() + "*")));
		return container;
	}

}
