package com.proit.todoApi.config;


import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import lombok.Getter;
import lombok.Setter;

@Configuration
@ConfigurationProperties("spring.datasource")
@Getter
@Setter
public class DBConfiguration {
	
	private final Logger LOGGER = LoggerFactory.getLogger(getClass());
	
	@Autowired
	private Environment env;
	
	private String driverClassName;
	private String url;
	private String username;
	private String password;
	
	private int minimumIdle;
	private int maximumPoolSize;
	private long idleTimeout;
	private String poolName;
	private long maxLifetime;
	private long connectionTimeout;
	
	@Profile("dev")
	@Bean
	public DataSource devDataSource(){
		
		LOGGER.info("DB connection for Development");
		
		return createDataSource();
	}

	@Profile("test")
	@Bean
	public DataSource testDataSource() {
		
		LOGGER.info("DB Connection fot Test");
		
		return createDataSource();
	}

	@Profile("prod")
	@Bean
	public DataSource prodDataSource(){
		LOGGER.info("DB Connection to Production");
		return createDataSource();
	}

	private DataSource createDataSource() {
		
		HikariConfig hikariConfig = new HikariConfig();
		hikariConfig.setDriverClassName(driverClassName);
	    hikariConfig.setJdbcUrl(url); 
	    hikariConfig.setUsername(username);
	    hikariConfig.setPassword(password);
	    
		hikariConfig.setMinimumIdle(minimumIdle); 
	    hikariConfig.setMaximumPoolSize(maximumPoolSize);
		hikariConfig.setIdleTimeout(idleTimeout); 
	    hikariConfig.setConnectionTestQuery("select 1 from dual");
		hikariConfig.setPoolName(poolName);
	    hikariConfig.setMaxLifetime(maxLifetime);
	    hikariConfig.setConnectionTimeout(connectionTimeout);
		
	    HikariDataSource dataSource = new HikariDataSource(hikariConfig);
	    
	    LOGGER.info("Diagnostic Data Source "+dataSource);
	    
	    return dataSource;
	}

}
