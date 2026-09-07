package com.github.laxika.magicalvibes.service.effect.staticfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.GainKeywordsOfCardsExiledWithSourceEffect;
import com.github.laxika.magicalvibes.service.effect.StaticBonusAccumulator;
import com.github.laxika.magicalvibes.service.effect.StaticEffectContext;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class GainKeywordsOfCardsExiledWithSourceSelfEffectHandler implements StaticEffectHandlerBean {

    private static final Set<Keyword> WATCHED_KEYWORDS = Set.of(
            Keyword.FLYING,
            Keyword.FIRST_STRIKE,
            Keyword.DOUBLE_STRIKE,
            Keyword.DEATHTOUCH,
            Keyword.HASTE,
            Keyword.HEXPROOF,
            Keyword.INDESTRUCTIBLE,
            Keyword.LIFELINK,
            Keyword.MENACE,
            Keyword.REACH,
            Keyword.TRAMPLE,
            Keyword.VIGILANCE
    );

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return GainKeywordsOfCardsExiledWithSourceEffect.class;
    }

    @Override
    public boolean selfOnly() {
        return true;
    }

    @Override
    public void apply(StaticEffectContext context, CardEffect effect, StaticBonusAccumulator accumulator) {
        for (Card card : context.gameData().getCardsExiledByPermanent(context.source().getId())) {
            for (Keyword keyword : card.getKeywords()) {
                if (WATCHED_KEYWORDS.contains(keyword)) {
                    accumulator.addKeyword(keyword);
                }
            }
        }
    }
}
