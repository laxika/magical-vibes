package com.github.laxika.magicalvibes.config;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonValue;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifePerGraveyardCardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordToTargetEffect;
import com.github.laxika.magicalvibes.model.effect.OpponentMayPlayCreatureEffect;
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
                .addMixIn(CardEffect.class, CardEffectMixin.class)
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

    @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "effectType")
    @JsonSubTypes({
            @JsonSubTypes.Type(value = AwardManaEffect.class, name = "AWARD_MANA"),
            @JsonSubTypes.Type(value = GainLifeEffect.class, name = "GAIN_LIFE"),
            @JsonSubTypes.Type(value = GainLifePerGraveyardCardEffect.class, name = "GAIN_LIFE_PER_GRAVEYARD_CARD"),
            @JsonSubTypes.Type(value = GrantKeywordToTargetEffect.class, name = "GRANT_KEYWORD_TO_TARGET"),
            @JsonSubTypes.Type(value = OpponentMayPlayCreatureEffect.class, name = "OPPONENT_MAY_PLAY_CREATURE")
    })
    static abstract class CardEffectMixin {
    }
}
