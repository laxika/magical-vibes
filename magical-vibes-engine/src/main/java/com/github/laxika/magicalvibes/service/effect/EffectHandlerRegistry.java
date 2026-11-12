package com.github.laxika.magicalvibes.service.effect;

import com.github.laxika.magicalvibes.model.effect.CardEffect;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Class-keyed registry of the handlers that resolve a {@link CardEffect} of a spell or ability. {@code GameEngineConfig}
 * registers every {@code NormalEffectHandlerBean} under its {@code handledEffect()} at startup, and
 * {@link #getHandler} looks a handler up by {@code effect.getClass()}, never by {@code instanceof}: an effect typed
 * by an interface needs one handler per concrete record.
 */
public class EffectHandlerRegistry {

    private final Map<Class<? extends CardEffect>, EffectHandler> handlers = new LinkedHashMap<>();

    public void register(Class<? extends CardEffect> effectType, EffectHandler handler) {
        handlers.put(effectType, handler);
    }

    public EffectHandler getHandler(CardEffect effect) {
        return handlers.get(effect.getClass());
    }
}

