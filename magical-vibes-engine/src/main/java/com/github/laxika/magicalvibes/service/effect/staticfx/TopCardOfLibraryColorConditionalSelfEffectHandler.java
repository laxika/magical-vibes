package com.github.laxika.magicalvibes.service.effect.staticfx;

import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.TopCardOfLibraryColorConditionalEffect;
import com.github.laxika.magicalvibes.service.effect.StaticBonusAccumulator;
import com.github.laxika.magicalvibes.service.effect.StaticEffectContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TopCardOfLibraryColorConditionalSelfEffectHandler implements StaticEffectHandlerBean {

    private final StaticEffectSupport support;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return TopCardOfLibraryColorConditionalEffect.class;
    }

    @Override
    public boolean selfOnly() {
        return true;
    }

    @Override
    public void apply(StaticEffectContext context, CardEffect effect, StaticBonusAccumulator accumulator) {
        var conditional = (TopCardOfLibraryColorConditionalEffect) effect;
        if (!support.isTopCardOfLibraryColor(context, conditional.color())) return;
        // Cards with "CARDNAME and other [type]" always buff themselves regardless of filter (CR 201.5).
        // The filter only applies to "other" creatures via the non-self handler.
        CardEffect wrapped = conditional.wrapped();
        if (wrapped instanceof StaticBoostEffect boost) {
            accumulator.addPower(boost.powerBoost());
            accumulator.addToughness(boost.toughnessBoost());
            accumulator.addKeywords(boost.grantedKeywords());
        } else if (wrapped instanceof GrantKeywordEffect grant) {
            accumulator.addKeywords(grant.keywords());
        }
    }
}
