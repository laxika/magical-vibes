package com.github.laxika.magicalvibes.service.effect;

import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.service.effect.mayfx.MayEffectHandlerBean;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Class-keyed registry of {@link MayEffectHandlerBean}s — the "you may …" analogue of
 * {@link EffectHandlerRegistry}. Populated at startup by {@code GameEngineConfig} and consulted by
 * {@code MayAbilityHandlerService.handleMayAbilityChosen}, which iterates a pending ability's effects
 * in list order and dispatches to the first one with a registered handler.
 */
public class MayEffectHandlerRegistry {

    private final Map<Class<? extends CardEffect>, MayEffectHandlerBean> handlers = new LinkedHashMap<>();

    public void register(Class<? extends CardEffect> effectType, MayEffectHandlerBean handler) {
        handlers.put(effectType, handler);
    }

    public MayEffectHandlerBean getHandler(CardEffect effect) {
        return handlers.get(effect.getClass());
    }
}
