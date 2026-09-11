package com.github.laxika.magicalvibes.service.effect.staticfx;

import com.github.laxika.magicalvibes.model.effect.BoostByAttackCountEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.service.effect.StaticBonusAccumulator;
import com.github.laxika.magicalvibes.service.effect.StaticEffectContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BoostByAttackCountEffectHandler implements StaticEffectHandlerBean {

    private final StaticEffectSupport support;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return BoostByAttackCountEffect.class;
    }

    @Override
    public void apply(StaticEffectContext context, CardEffect effect, StaticBonusAccumulator accumulator) {
        var boost = (BoostByAttackCountEffect) effect;
        if (!support.matchesCreatureScope(context, boost.scope(), boost.filter())) {
            return;
        }
        int attacks = context.target().getAttacksThisTurn();
        accumulator.addPower(attacks * boost.powerPerAttack());
        accumulator.addToughness(attacks * boost.toughnessPerAttack());
    }
}
