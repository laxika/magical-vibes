package com.github.laxika.magicalvibes.service.effect.staticfx;

import com.github.laxika.magicalvibes.model.effect.BoostByAttackCountEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
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
        boolean inScope = boost.scope() == GrantScope.SELF
                ? context.target().getId().equals(context.sourceId())
                        && support.matchesStaticFilter(context, context.target(), boost.filter())
                : support.matchesCreatureScope(context, boost.scope(), boost.filter());
        if (!inScope) {
            return;
        }
        int attacks = boost.countThisGame()
                ? context.target().getAttacksThisGame()
                : context.target().getAttacksThisTurn();
        accumulator.addPower(attacks * boost.powerPerAttack());
        accumulator.addToughness(attacks * boost.toughnessPerAttack());
    }
}
