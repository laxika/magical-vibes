package com.github.laxika.magicalvibes.service.effect.staticfx;

import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.service.effect.StaticBonusAccumulator;
import com.github.laxika.magicalvibes.service.effect.StaticEffectContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Self pass for {@link GrantKeywordEffect}: grants the keyword(s) to the source permanent itself
 * when the effect's subject names it, filter permitting ("this creature", "creatures you control" while it is a
 * creature, "each land" while it is a land; never "other permanents you control",
 * {@link StaticEffectSupport#withOwnPermanentsAsOthers}). The non-self
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
        if (support.names(context,
                StaticEffectSupport.withOwnPermanentsAsOthers(grant.scope()), grant.filter())) {
            accumulator.addKeywords(grant.keywords());
        }
    }
}
