package com.github.laxika.magicalvibes.service.effect.staticfx;

import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ControllerLifeThresholdConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.service.effect.StaticBonusAccumulator;
import com.github.laxika.magicalvibes.service.effect.StaticEffectContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ControllerLifeThresholdConditionalSelfEffectHandler implements StaticEffectHandlerBean {

    private final StaticEffectSupport support;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ControllerLifeThresholdConditionalEffect.class;
    }

    @Override
    public boolean selfOnly() {
        return true;
    }

    @Override
    public void apply(StaticEffectContext context, CardEffect effect, StaticBonusAccumulator accumulator) {
        var conditional = (ControllerLifeThresholdConditionalEffect) effect;
        UUID controllerId = support.findControllerId(context.gameData(), context.source());
        if (controllerId == null) return;
        int lifeTotal = context.gameData().playerLifeTotals.getOrDefault(controllerId, 20);
        if (lifeTotal >= conditional.lifeThreshold()) {
            CardEffect wrapped = conditional.wrapped();
            if (wrapped instanceof StaticBoostEffect boost) {
                accumulator.addPower(boost.powerBoost());
                accumulator.addToughness(boost.toughnessBoost());
                accumulator.addKeywords(boost.grantedKeywords());
            } else if (wrapped instanceof GrantKeywordEffect grant) {
                if (grant.scope() == GrantScope.SELF || support.matchesStaticFilter(context.target(), grant.filter())) {
                    accumulator.addKeywords(grant.keywords());
                }
            }
        }
    }
}
