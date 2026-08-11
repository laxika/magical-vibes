package com.github.laxika.magicalvibes.service.effect.manafx;

import com.github.laxika.magicalvibes.model.effect.CardEffect;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class ManaAbilityEffectHandlerRegistry {

    private final Map<Class<? extends CardEffect>, ManaAbilityEffectHandler> handlers = new LinkedHashMap<>();

    public ManaAbilityEffectHandlerRegistry(List<ManaAbilityEffectHandler> handlers) {
        for (ManaAbilityEffectHandler handler : handlers) {
            this.handlers.put(handler.handledEffect(), handler);
        }
    }

    public ManaAbilityEffectHandler getHandler(CardEffect effect) {
        return handlers.get(effect.getClass());
    }

    public boolean isRevertable(CardEffect effect) {
        ManaAbilityEffectHandler handler = getHandler(effect);
        return handler != null && handler.isRevertable();
    }
}
