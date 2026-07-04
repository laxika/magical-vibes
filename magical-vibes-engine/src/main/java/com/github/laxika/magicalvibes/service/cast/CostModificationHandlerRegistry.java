package com.github.laxika.magicalvibes.service.cast;

import com.github.laxika.magicalvibes.model.effect.CardEffect;

import java.util.LinkedHashMap;
import java.util.Map;

public class CostModificationHandlerRegistry {

    private final Map<Class<? extends CardEffect>, CostModificationHandlerBean> battlefieldHandlers = new LinkedHashMap<>();
    private final Map<Class<? extends CardEffect>, CostModificationHandlerBean> spellSelfHandlers = new LinkedHashMap<>();

    public void register(CostModificationHandlerBean handler) {
        if (handler.onSpellItself()) {
            spellSelfHandlers.put(handler.handledEffect(), handler);
        } else {
            battlefieldHandlers.put(handler.handledEffect(), handler);
        }
    }

    public CostModificationHandlerBean getBattlefieldHandler(CardEffect effect) {
        return battlefieldHandlers.get(effect.getClass());
    }

    public CostModificationHandlerBean getSpellSelfHandler(CardEffect effect) {
        return spellSelfHandlers.get(effect.getClass());
    }
}
