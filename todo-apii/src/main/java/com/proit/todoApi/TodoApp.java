package com.proit.todoApi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan({"com.proit.todoApi"})
public class TodoApp extends SpringBootServletInitializer {

	public static void main(String[] args) {
		System.out.println( "Start.........!" );
		SpringApplication.run(TodoApp.class, args);
		System.out.println( "End.........!" );
	}

}
