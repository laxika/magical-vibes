package com.github.laxika.magicalvibes.service.effect.staticfx;

import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.LosesAllNonManaAbilitiesEffect;
import com.github.laxika.magicalvibes.service.effect.StaticBonusAccumulator;
import com.github.laxika.magicalvibes.service.effect.StaticEffectContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Applies a targeted self-scoped non-mana ability removal from a granted static effect. */
@Component
@RequiredArgsConstructor
public class LosesAllNonManaAbilitiesSelfEffectHandler implements StaticEffectHandlerBean {

    private final StaticEffectSupport support;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return LosesAllNonManaAbilitiesEffect.class;
    }

    @Override
    public boolean selfOnly() {
        return true;
    }

    @Override
    public void apply(StaticEffectContext context, CardEffect effect, StaticBonusAccumulator accumulator) {
        var loses = (LosesAllNonManaAbilitiesEffect) effect;
        if ((loses.scope() == GrantScope.SELF || loses.scope() == GrantScope.SELF_AND_PAIRED)
                && support.matchesStaticFilter(context, context.target(), loses.filter())) {
            accumulator.setLosesAllNonManaAbilities(true);
        }
    }
}
