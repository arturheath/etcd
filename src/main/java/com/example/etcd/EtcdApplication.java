package com.example.etcd;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootConfiguration
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class EtcdApplication {

	public static void main(String[] args) {
		SpringApplication.run(EtcdApplication.class, args);
	}

}
