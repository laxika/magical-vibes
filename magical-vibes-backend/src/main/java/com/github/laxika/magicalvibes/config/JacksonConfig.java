package com.github.laxika.magicalvibes.config;

import com.fasterxml.jackson.annotation.JsonValue;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

@Configuration
public class JacksonConfig {

    @Bean
    public ObjectMapper objectMapper() {
        return JsonMapper.builder()
                .addMixIn(CardType.class, CardTypeMixin.class)
                .addMixIn(CardSubtype.class, CardSubtypeMixin.class)
                .build();
    }

    static abstract class CardTypeMixin {
        @JsonValue
        abstract String getDisplayName();
    }

    static abstract class CardSubtypeMixin {
        @JsonValue
        abstract String getDisplayName();
    }
}
