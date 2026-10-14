package com.github.laxika.magicalvibes.service.effect.staticfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.GainKeywordsOfCreatureCardsExiledWithSourceEffect;
import com.github.laxika.magicalvibes.model.effect.ProtectionGrantingEffect;
import com.github.laxika.magicalvibes.model.effect.TargetingRestrictionEffect;
import com.github.laxika.magicalvibes.service.effect.StaticBonusAccumulator;
import com.github.laxika.magicalvibes.service.effect.StaticEffectContext;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

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
        for (Card card : candidateCards(context, keywordEffect)) {
            if (!card.hasType(CardType.CREATURE)) {
                continue;
            }
            if (keywordEffect.requiredExiledCardCounter() != null
                    && (keywordEffect.requiredExiledCardCounter() != CounterType.BLOOD
                    || !context.gameData().exiledCardsWithBloodCounters.contains(card.getId()))) {
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
                    } else if (staticEffect instanceof TargetingRestrictionEffect restriction
                            && restriction.hexproofLike()
                            && keywordEffect.watchedKeywords().contains(Keyword.HEXPROOF)) {
                        accumulator.addGrantedEffect(staticEffect);
                    }
                }
            }
        }
    }

    /**
     * Cards exiled with the source, or - when a blood counter is required - every face-up exiled card
     * regardless of which permanent or player exiled it (Rayami looks at all exiled creature cards with
     * blood counters).
     */
    private List<Card> candidateCards(StaticEffectContext context,
                                      GainKeywordsOfCreatureCardsExiledWithSourceEffect keywordEffect) {
        if (keywordEffect.requiredExiledCardCounter() != CounterType.BLOOD) {
            return context.gameData().getCardsExiledByPermanent(context.sourceId());
        }
        List<Card> cards = new ArrayList<>();
        synchronized (context.gameData().exiledCards) {
            for (ExiledCardEntry entry : context.gameData().exiledCards) {
                if (!entry.faceDown()) {
                    cards.add(entry.card());
                }
            }
        }
        return cards;
    }
}
