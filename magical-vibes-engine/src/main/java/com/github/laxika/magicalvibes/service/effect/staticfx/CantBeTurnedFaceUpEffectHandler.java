package com.github.laxika.magicalvibes.service.effect.staticfx;

import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CantBeTurnedFaceUpEffect;
import com.github.laxika.magicalvibes.service.effect.StaticBonusAccumulator;
import com.github.laxika.magicalvibes.service.effect.StaticEffectContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CantBeTurnedFaceUpEffectHandler implements StaticEffectHandlerBean {

    private final StaticEffectSupport support;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return CantBeTurnedFaceUpEffect.class;
    }

    @Override
    public void apply(StaticEffectContext context, CardEffect effect, StaticBonusAccumulator accumulator) {
        CantBeTurnedFaceUpEffect restriction = (CantBeTurnedFaceUpEffect) effect;
        if (context.target().isFaceDown()
                && support.matchesCreatureScope(context, restriction.scope(), null)) {
            accumulator.setTurnFaceUpPrevented(true);
        }
    }
}
