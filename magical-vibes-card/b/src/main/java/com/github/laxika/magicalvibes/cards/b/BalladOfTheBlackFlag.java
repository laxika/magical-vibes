package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MillControllerAndMayReturnMilledPermanentToHandEffect;
import com.github.laxika.magicalvibes.model.effect.ReduceCastCostForMatchingSpellsUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.filter.CardIsHistoricPredicate;

@CardRegistration(set = "ACR", collectorNumber = "13")
public class BalladOfTheBlackFlag extends Card {

    public BalladOfTheBlackFlag() {
        CardIsHistoricPredicate historic = new CardIsHistoricPredicate();

        addEffect(EffectSlot.SAGA_CHAPTER_I,
                new MillControllerAndMayReturnMilledPermanentToHandEffect(3, historic));
        addEffect(EffectSlot.SAGA_CHAPTER_II,
                new MillControllerAndMayReturnMilledPermanentToHandEffect(3, historic));
        addEffect(EffectSlot.SAGA_CHAPTER_III,
                new MillControllerAndMayReturnMilledPermanentToHandEffect(3, historic));
        addEffect(EffectSlot.SAGA_CHAPTER_IV,
                new ReduceCastCostForMatchingSpellsUntilEndOfTurnEffect(
                        historic, 2));
    }
}
