package org.looksworking.oauth.client;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;

@SpringBootApplication
public class Oauth2InActionClientApplication {

	public static void main(String[] args) {
		SpringApplication.run(Oauth2InActionClientApplication.class, args);
	}

	@Bean
	@Scope("session")
	public ModelVars modelVars(){
		return new ModelVars();
	}

}
