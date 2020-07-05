package com.albedo.java.common.config;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

@Configuration
public class JacksonMapperConfig {

	@Bean
	public ObjectMapper myObjectMapper(Jackson2ObjectMapperBuilder builder) {
		return builder.createXmlMapper(false).modules(new CustomFieldModule())
			.featuresToEnable(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES).build();
	}

}
