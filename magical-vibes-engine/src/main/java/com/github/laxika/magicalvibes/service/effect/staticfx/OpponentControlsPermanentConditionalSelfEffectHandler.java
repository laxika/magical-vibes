package com.github.laxika.magicalvibes.service.effect.staticfx;

import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.OpponentControlsPermanentConditionalEffect;
import com.github.laxika.magicalvibes.service.effect.StaticBonusAccumulator;
import com.github.laxika.magicalvibes.service.effect.StaticEffectContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class OpponentControlsPermanentConditionalSelfEffectHandler implements StaticEffectHandlerBean {

    private final StaticEffectSupport support;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return OpponentControlsPermanentConditionalEffect.class;
    }

    @Override
    public boolean selfOnly() {
        return true;
    }

    @Override
    public void apply(StaticEffectContext context, CardEffect effect, StaticBonusAccumulator accumulator) {
        var conditional = (OpponentControlsPermanentConditionalEffect) effect;
        UUID controllerId = support.findControllerId(context.gameData(), context.source());
        if (controllerId == null) return;
        final boolean[] found = {false};
        context.gameData().forEachPermanent((playerId, permanent) -> {
            if (!found[0]
                    && !playerId.equals(controllerId)
                    && support.matchesStaticFilter(permanent, conditional.filter())) {
                found[0] = true;
            }
        });
        if (found[0]) {
            support.applySelfOnlyConditionalStaticEffect(context, conditional.wrapped(), accumulator);
        }
    }
}
