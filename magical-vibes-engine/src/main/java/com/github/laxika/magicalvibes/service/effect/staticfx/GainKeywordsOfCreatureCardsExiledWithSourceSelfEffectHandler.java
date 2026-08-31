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

@Component
public class GainKeywordsOfCreatureCardsExiledWithSourceSelfEffectHandler implements StaticEffectHandlerBean {

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
        GainKeywordsOfCreatureCardsExiledWithSourceEffect keywordEffect =
                (GainKeywordsOfCreatureCardsExiledWithSourceEffect) effect;
        for (Card card : context.gameData().getCardsExiledByPermanent(context.source().getId())) {
            if (!card.hasType(CardType.CREATURE)) {
                continue;
            }

            for (Keyword keyword : card.getKeywords()) {
                if (keywordEffect.watchedKeywords().contains(keyword)) {
                    accumulator.addKeyword(keyword);
                }
            }

            if (keywordEffect.copyProtectionEffects()) {
                for (CardEffect staticEffect : card.getEffects(EffectSlot.STATIC)) {
                    if (staticEffect instanceof ProtectionGrantingEffect protection
                            && protection.protectionScope() == null) {
                        accumulator.addGrantedEffect(staticEffect);
                    }
                }
            }
        }
    }
}
