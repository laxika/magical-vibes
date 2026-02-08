package com.github.laxika.magicalvibes.model.effect;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "effectType")
@JsonSubTypes({
        @JsonSubTypes.Type(value = AwardManaEffect.class, name = "AWARD_MANA")
})
public interface CardEffect {
}
