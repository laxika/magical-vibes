package com.github.laxika.magicalvibes.service.effect;

import com.github.laxika.magicalvibes.model.effect.CardEffect;

import java.util.LinkedHashMap;
import java.util.Map;

public class TargetValidatorRegistry {

    private final Map<Class<? extends CardEffect>, TargetValidator> validators = new LinkedHashMap<>();

    public void register(Class<? extends CardEffect> effectType, TargetValidator validator) {
        validators.put(effectType, validator);
    }

    public TargetValidator getValidator(CardEffect effect) {
        return validators.get(effect.getClass());
    }
}
