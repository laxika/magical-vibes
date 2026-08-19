package com.github.laxika.magicalvibes.service.effect.staticfx;

import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
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
        boolean matches = switch (loses.scope()) {
            case OWN_LANDS, OPPONENT_LANDS, ALL_LANDS, ALL_LANDS_INCLUDING_SELF ->
                    support.matchesLandScope(context, loses.scope(), loses.filter());
            default -> support.matchesCreatureScope(context, loses.scope(), loses.filter());
        };
        if (matches) {
            accumulator.setLosesAllNonManaAbilities(true);
        }
    }
}
