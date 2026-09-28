package com.github.laxika.magicalvibes.service.effect.staticfx;

import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.LosesAllAbilitiesEffect;
import com.github.laxika.magicalvibes.service.effect.StaticBonusAccumulator;
import com.github.laxika.magicalvibes.service.effect.StaticEffectContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Applies a self-scoped lose-all-abilities static effect from a runtime card. */
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
        if ((loses.scope() == GrantScope.SELF || loses.scope() == GrantScope.SELF_AND_PAIRED)
                && support.matchesStaticFilter(context, context.target(), loses.filter())) {
            accumulator.setLosesAllAbilities(true);
        }
    }
}
