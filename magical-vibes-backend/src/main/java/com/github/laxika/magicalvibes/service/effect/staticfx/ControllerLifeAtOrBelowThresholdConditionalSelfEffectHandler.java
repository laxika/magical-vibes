package com.github.laxika.magicalvibes.service.effect.staticfx;

import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ControllerLifeAtOrBelowThresholdConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.service.effect.StaticBonusAccumulator;
import com.github.laxika.magicalvibes.service.effect.StaticEffectContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ControllerLifeAtOrBelowThresholdConditionalSelfEffectHandler implements StaticEffectHandlerBean {

    private final StaticEffectSupport support;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ControllerLifeAtOrBelowThresholdConditionalEffect.class;
    }

    @Override
    public boolean selfOnly() {
        return true;
    }

    @Override
    public void apply(StaticEffectContext context, CardEffect effect, StaticBonusAccumulator accumulator) {
        var conditional = (ControllerLifeAtOrBelowThresholdConditionalEffect) effect;
        if (!support.isControllerLifeAtOrBelow(context, conditional.lifeThreshold())) return;
        CardEffect wrapped = conditional.wrapped();
        if (wrapped instanceof StaticBoostEffect boost) {
            if (boost.scope() == GrantScope.SELF || boost.scope() == GrantScope.ALL_OWN_CREATURES) {
                accumulator.addPower(boost.powerBoost());
                accumulator.addToughness(boost.toughnessBoost());
                accumulator.addKeywords(boost.grantedKeywords());
            }
        } else if (wrapped instanceof GrantKeywordEffect grant) {
            if (grant.scope() == GrantScope.SELF || support.matchesStaticFilter(context.target(), grant.filter())) {
                accumulator.addKeywords(grant.keywords());
            }
        }
    }
}
