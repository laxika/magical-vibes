package com.github.laxika.magicalvibes.service.effect.staticfx;

import com.github.laxika.magicalvibes.model.effect.AnyPlayerControlsPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.service.effect.StaticBonusAccumulator;
import com.github.laxika.magicalvibes.service.effect.StaticEffectContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AnyPlayerControlsPermanentConditionalSelfEffectHandler implements StaticEffectHandlerBean {

    private final StaticEffectSupport support;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return AnyPlayerControlsPermanentConditionalEffect.class;
    }

    @Override
    public boolean selfOnly() {
        return true;
    }

    @Override
    public void apply(StaticEffectContext context, CardEffect effect, StaticBonusAccumulator accumulator) {
        var conditional = (AnyPlayerControlsPermanentConditionalEffect) effect;
        final boolean[] found = {false};
        context.gameData().forEachPermanent((playerId, permanent) -> {
            if (!found[0] && support.matchesStaticFilter(permanent, conditional.filter())) {
                found[0] = true;
            }
        });
        if (found[0]) {
            support.applySelfOnlyConditionalStaticEffect(context, conditional.wrapped(), accumulator);
        }
    }
}
