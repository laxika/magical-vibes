package com.github.laxika.magicalvibes.service.effect.staticfx;

import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EnchantedPermanentConditionalEffect;
import com.github.laxika.magicalvibes.service.effect.StaticBonusAccumulator;
import com.github.laxika.magicalvibes.service.effect.StaticEffectContext;
import com.github.laxika.magicalvibes.service.effect.StaticEffectHandler;
import com.github.laxika.magicalvibes.service.effect.StaticEffectHandlerRegistry;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EnchantedPermanentConditionalEffectHandler implements StaticEffectHandlerBean {

    private final StaticEffectSupport support;
    private final StaticEffectHandlerRegistry staticEffectHandlerRegistry;
    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return EnchantedPermanentConditionalEffect.class;
    }

    @Override
    public void apply(StaticEffectContext context, CardEffect effect, StaticBonusAccumulator accumulator) {
        var conditional = (EnchantedPermanentConditionalEffect) effect;
        if (!context.source().isAttached()) {
            return;
        }
        var enchanted = gameQueryService.findPermanentById(
                context.gameData(), context.source().getAttachedTo());
        if (enchanted == null) {
            return;
        }
        CardEffect activeEffect = support.matchesStaticFilter(context, enchanted, conditional.filter())
                ? conditional.ifMatch()
                : conditional.ifNotMatch();
        if (activeEffect == null) {
            // Single-branch conditional (null ifMatch/ifNotMatch): the inactive branch does nothing.
            return;
        }
        StaticEffectHandler handler = staticEffectHandlerRegistry.getHandler(activeEffect);
        if (handler != null) {
            handler.apply(context, activeEffect, accumulator);
        }
    }
}
