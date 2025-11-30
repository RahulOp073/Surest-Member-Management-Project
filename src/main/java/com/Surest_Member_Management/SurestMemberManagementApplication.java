package com.Surest_Member_Management;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;



@SpringBootApplication(scanBasePackages = "com.Surest_Member_Management")
@EnableCaching
public class SurestMemberManagementApplication {

	public static void main(String[] args) {

        SpringApplication.run(SurestMemberManagementApplication.class, args);
	}

}
