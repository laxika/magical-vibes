package com.github.laxika.magicalvibes.service.effect;

import com.github.laxika.magicalvibes.model.effect.CardEffect;

import java.util.LinkedHashMap;
import java.util.Map;

public class StaticEffectHandlerRegistry {

    private final Map<Class<? extends CardEffect>, StaticEffectHandler> handlers = new LinkedHashMap<>();

    public void register(Class<? extends CardEffect> effectType, StaticEffectHandler handler) {
        handlers.put(effectType, handler);
    }

    public StaticEffectHandler getHandler(CardEffect effect) {
        return handlers.get(effect.getClass());
    }
}
