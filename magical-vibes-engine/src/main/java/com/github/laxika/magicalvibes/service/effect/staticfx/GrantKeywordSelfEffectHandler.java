package com.github.laxika.magicalvibes.service.effect.staticfx;

import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.service.effect.StaticBonusAccumulator;
import com.github.laxika.magicalvibes.service.effect.StaticEffectContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Self pass for {@link GrantKeywordEffect}: grants the keyword(s) to the source permanent itself
 * when the scope covers it ({@link GrantScope#SELF}, {@link GrantScope#SELF_AND_PAIRED},
 * {@link GrantScope#ALL_OWN_CREATURES}, or {@link GrantScope#ALL_CREATURES_INCLUDING_SELF},
 * filter permitting). The non-self
 * {@link GrantKeywordEffectHandler} already covers other creatures — e.g. Sun Quan, Lord of Wu
 * grants horsemanship to himself as well as the rest of your board.
 */
@Component
@RequiredArgsConstructor
public class GrantKeywordSelfEffectHandler implements StaticEffectHandlerBean {

    private final StaticEffectSupport support;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return GrantKeywordEffect.class;
    }

    @Override
    public boolean selfOnly() {
        return true;
    }

    @Override
    public void apply(StaticEffectContext context, CardEffect effect, StaticBonusAccumulator accumulator) {
        var grant = (GrantKeywordEffect) effect;
        if ((grant.scope() == GrantScope.SELF || grant.scope() == GrantScope.SELF_AND_PAIRED
                || grant.scope() == GrantScope.ALL_OWN_CREATURES
                || grant.scope() == GrantScope.ALL_CREATURES_INCLUDING_SELF)
                && support.matchesStaticFilter(context, context.target(), grant.filter())) {
            accumulator.addKeywords(grant.keywords());
        }
    }
}
