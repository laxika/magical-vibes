package com.github.laxika.magicalvibes.service.effect.staticfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.GainKeywordsOfCreatureCardsExiledWithSourceEffect;
import com.github.laxika.magicalvibes.model.effect.ProtectionGrantingEffect;
import com.github.laxika.magicalvibes.service.effect.StaticBonusAccumulator;
import com.github.laxika.magicalvibes.service.effect.StaticEffectContext;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class GainKeywordsOfCreatureCardsExiledWithSourceSelfEffectHandler implements StaticEffectHandlerBean {

    private static final Set<Keyword> WATCHED_KEYWORDS = Set.of(
            Keyword.FLYING,
            Keyword.FEAR,
            Keyword.FIRST_STRIKE,
            Keyword.DOUBLE_STRIKE,
            Keyword.HASTE,
            Keyword.FORESTWALK,
            Keyword.MOUNTAINWALK,
            Keyword.ISLANDWALK,
            Keyword.SWAMPWALK,
            Keyword.PLAINSWALK,
            Keyword.TRAMPLE
    );

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return GainKeywordsOfCreatureCardsExiledWithSourceEffect.class;
    }

    @Override
    public boolean selfOnly() {
        return true;
    }

    @Override
    public void apply(StaticEffectContext context, CardEffect effect, StaticBonusAccumulator accumulator) {
        for (Card card : context.gameData().getCardsExiledByPermanent(context.source().getId())) {
            if (!card.hasType(CardType.CREATURE)) {
                continue;
            }

            for (Keyword keyword : card.getKeywords()) {
                if (WATCHED_KEYWORDS.contains(keyword)) {
                    accumulator.addKeyword(keyword);
                }
            }

            for (CardEffect staticEffect : card.getEffects(EffectSlot.STATIC)) {
                if (staticEffect instanceof ProtectionGrantingEffect protection
                        && protection.protectionScope() == null) {
                    accumulator.addGrantedEffect(staticEffect);
                }
            }
        }
    }
}
