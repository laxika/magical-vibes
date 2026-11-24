package com.github.laxika.magicalvibes.service.effect.staticfx;

import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.LosesAllAbilitiesEffect;
import com.github.laxika.magicalvibes.service.effect.StaticBonusAccumulator;
import com.github.laxika.magicalvibes.service.effect.StaticEffectContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Applies a lose-all-abilities static effect to its own source (self scopes, or a creature-wide scope that includes it). */
@Component
@RequiredArgsConstructor
public class LosesAllAbilitiesSelfEffectHandler implements StaticEffectHandlerBean {

    private final StaticEffectSupport support;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return LosesAllAbilitiesEffect.class;
    }

    @Override
    public boolean selfOnly() {
        return true;
    }

    @Override
    public void apply(StaticEffectContext context, CardEffect effect, StaticBonusAccumulator accumulator) {
        var loses = (LosesAllAbilitiesEffect) effect;
        boolean applies = support.names(context, loses.subject(), loses.filter());
        if (applies) {
            if (loses.exceptManaAbilities()) {
                accumulator.setLosesAllNonManaAbilities(true);
            } else {
                accumulator.setLosesAllAbilities(true);
            }
        }
    }
}
