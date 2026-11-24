package com.github.laxika.magicalvibes.service.effect.staticfx;

import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.GainKeywordsOfCreatureCardsExiledWithSourceEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.StaticBonusAccumulator;
import com.github.laxika.magicalvibes.service.effect.StaticEffectContext;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Gives a recipient other than the source (the equipped creature of Eater of Virtue) the watched keywords of the
 * cards a {@link GainKeywordsOfCreatureCardsExiledWithSourceEffect} names, the cards read for the source.
 */
@Component
@RequiredArgsConstructor
public class GainKeywordsOfCreatureCardsExiledWithSourceEffectHandler implements StaticEffectHandlerBean {

    private final StaticEffectSupport support;
    private final GameQueryService gameQueryService;
    private final PredicateEvaluationService predicateEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return GainKeywordsOfCreatureCardsExiledWithSourceEffect.class;
    }

    @Override
    public void apply(StaticEffectContext context, CardEffect effect, StaticBonusAccumulator accumulator) {
        var keywordEffect = (GainKeywordsOfCreatureCardsExiledWithSourceEffect) effect;
        if (support.namesBeyondSource(context, keywordEffect.recipient(), null)) {
            GainKeywordsOfCreatureCardsExiledWithSourceSelfEffectHandler.lend(context, keywordEffect, accumulator,
                    gameQueryService, predicateEvaluationService);
        }
    }
}
