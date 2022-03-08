package com.example.workflow.config;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.web.client.RestTemplate;


@Configuration
public class DatabaseConfiguration {
	
	@Bean(name="primaryDatasource")
	@Primary
	@ConfigurationProperties(prefix="primary.datasource")
	public DataSource primaryDataSource() {
	  return DataSourceBuilder.create().build();
	}

	@Bean(name="camundaBpmDataSource")
	@ConfigurationProperties(prefix="camunda.datasource")
	public DataSource secondaryDataSource() {
	  return DataSourceBuilder.create().build();
	}
	
	@Bean
	public PlatformTransactionManager transactionManager(@Qualifier("camundaBpmDataSource") DataSource dataSource) {
	  return new DataSourceTransactionManager(dataSource);
	}
	
	@Bean
	public PlatformTransactionManager primaryTxnManager(@Qualifier("primaryDatasource") DataSource dataSource) {
	  return new DataSourceTransactionManager(dataSource);
	}
	
	@Bean
	public RestTemplate restTemplate() {
		return new RestTemplate();
	}
	
}