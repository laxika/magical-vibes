package com.github.laxika.magicalvibes.service.effect.staticfx;

import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.service.effect.StaticBonusAccumulator;
import com.github.laxika.magicalvibes.service.effect.StaticEffectContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component("staticGrantKeywordEffectHandler")
@RequiredArgsConstructor
public class GrantKeywordEffectHandler implements StaticEffectHandlerBean {

    private final StaticEffectSupport support;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return GrantKeywordEffect.class;
    }

    @Override
    public void apply(StaticEffectContext context, CardEffect effect, StaticBonusAccumulator accumulator) {
        var grant = (GrantKeywordEffect) effect;
        boolean scopeMatch = support.names(context,
                StaticEffectSupport.withOwnPermanentsAsOthers(grant.scope()), grant.filter());
        if (scopeMatch) {
            accumulator.addKeywords(grant.keywords());
        }
    }
}
