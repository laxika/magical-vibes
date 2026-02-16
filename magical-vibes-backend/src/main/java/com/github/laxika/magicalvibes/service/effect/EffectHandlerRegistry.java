package com.github.laxika.magicalvibes.service.effect;

import com.github.laxika.magicalvibes.model.effect.CardEffect;

import java.util.LinkedHashMap;
import java.util.Map;

public class EffectHandlerRegistry {

    private final Map<Class<? extends CardEffect>, EffectHandler> handlers = new LinkedHashMap<>();

    public void register(Class<? extends CardEffect> effectType, EffectHandler handler) {
        handlers.put(effectType, handler);
    }

    public EffectHandler getHandler(CardEffect effect) {
        return handlers.get(effect.getClass());
    }
}
