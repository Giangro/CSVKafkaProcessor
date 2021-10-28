/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.poste.csvkafkaprocessor.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.support.DefaultKafkaHeaderMapper;
import org.springframework.kafka.support.SimpleKafkaHeaderMapper;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 *
 * @author GIANGR40
 */
@Configuration
@EnableTransactionManagement
@EnableRetry
@ComponentScan(basePackages = "it.poste")
@Slf4j
public class Config {

    @Bean
    public SimpleKafkaHeaderMapper defaultKafkaHeaderMapper() {
        SimpleKafkaHeaderMapper defaultKafkaHeaderMapper
                = new SimpleKafkaHeaderMapper();        
     
        defaultKafkaHeaderMapper.setMapAllStringsOut​(true);
        //defaultKafkaHeaderMapper.setEncodeStrings(true);
        return defaultKafkaHeaderMapper;
    }
     
}
