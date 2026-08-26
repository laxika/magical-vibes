package com.github.laxika.magicalvibes.service.effect;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** Class-keyed dispatch for replacement effects applied while a permanent enters the battlefield. */
@Component
public class EntryReplacementHandlerRegistry {

    private final Map<Class<? extends CardEffect>, EntryReplacementHandlerBean> handlers;

    public EntryReplacementHandlerRegistry(List<EntryReplacementHandlerBean> handlerBeans) {
        Map<Class<? extends CardEffect>, EntryReplacementHandlerBean> handlers = new HashMap<>();
        for (EntryReplacementHandlerBean handler : handlerBeans) {
            handlers.put(handler.handledEffect(), handler);
        }
        this.handlers = Map.copyOf(handlers);
    }

    public void apply(GameData gameData, UUID controllerId, Permanent enteringPermanent,
                      CardEffect effect) {
        EntryReplacementHandlerBean handler = handlers.get(effect.getClass());
        if (handler != null) {
            handler.apply(gameData, controllerId, enteringPermanent, effect);
        }
    }
}
