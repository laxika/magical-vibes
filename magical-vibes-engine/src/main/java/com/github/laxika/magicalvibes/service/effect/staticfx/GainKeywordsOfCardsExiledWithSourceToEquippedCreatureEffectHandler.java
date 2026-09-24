package com.github.laxika.magicalvibes.service.effect.staticfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.GainKeywordsOfCardsExiledWithSourceToEquippedCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.ProtectionGrantingEffect;
import com.github.laxika.magicalvibes.service.effect.StaticBonusAccumulator;
import com.github.laxika.magicalvibes.service.effect.StaticEffectContext;
import org.springframework.stereotype.Component;

@Component
public class GainKeywordsOfCardsExiledWithSourceToEquippedCreatureEffectHandler
        implements StaticEffectHandlerBean {

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return GainKeywordsOfCardsExiledWithSourceToEquippedCreatureEffect.class;
    }

    @Override
    public void apply(StaticEffectContext context, CardEffect effect, StaticBonusAccumulator accumulator) {
        if (!context.source().isAttached()
                || !context.source().getAttachedTo().equals(context.target().getId())) {
            return;
        }

        var keywordEffect = (GainKeywordsOfCardsExiledWithSourceToEquippedCreatureEffect) effect;
        for (Card card : context.gameData().getCardsExiledByPermanent(context.source().getId())) {
            for (Keyword keyword : card.getKeywords()) {
                if (keywordEffect.keywords().contains(keyword)) {
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
