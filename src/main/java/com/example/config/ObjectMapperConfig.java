package com.example.config;

import com.example.model.Resource;
import com.example.service.FileBasedResourceAclService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Set;
import org.springframework.cache.concurrent.ConcurrentMapCache;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ObjectMapperConfig {
  @Bean
  public ObjectMapper objectMapper() {
    return new ObjectMapper();
  }

  @Bean
  public ConcurrentMapCache concurrentMapCache() {
    ConcurrentMapCache cache = new ConcurrentMapCache("cache");
    cache.put("rs", Resource.builder().id("rs").build());
    cache.put(
        "rs:" + FileBasedResourceAclService.RESOURCE_ID_TO_ID_OF_USER_ALLOWED_TO_READ_KEY,
        Set.of(1));
    return cache;
  }
}
