package com.github.laxika.magicalvibes.service.effect.staticfx;

import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.LosesAllNonManaAbilitiesEffect;
import com.github.laxika.magicalvibes.service.effect.StaticBonusAccumulator;
import com.github.laxika.magicalvibes.service.effect.StaticEffectContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component("staticLosesAllNonManaAbilitiesEffectHandler")
@RequiredArgsConstructor
public class LosesAllNonManaAbilitiesEffectHandler implements StaticEffectHandlerBean {

    private final StaticEffectSupport support;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return LosesAllNonManaAbilitiesEffect.class;
    }

    @Override
    public void apply(StaticEffectContext context, CardEffect effect, StaticBonusAccumulator accumulator) {
        var loses = (LosesAllNonManaAbilitiesEffect) effect;
        boolean matches = support.namesBeyondSource(context, loses.subject(), loses.filter());
        if (matches) {
            accumulator.setLosesAllNonManaAbilities(true);
        }
    }
}
