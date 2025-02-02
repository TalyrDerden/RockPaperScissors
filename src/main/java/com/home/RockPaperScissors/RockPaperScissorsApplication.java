package com.home.RockPaperScissors;

import com.home.RockPaperScissors.service.GameServer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;


@SpringBootApplication
public class RockPaperScissorsApplication {

	public static void main(String[] args) {
		SpringApplication.run(RockPaperScissorsApplication.class, args);
	}

	@Bean
	public GameServer gameServer() {
		return new GameServer();
	}
}
