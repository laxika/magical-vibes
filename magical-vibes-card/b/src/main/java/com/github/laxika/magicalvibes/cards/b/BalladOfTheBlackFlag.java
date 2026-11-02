package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.MillControllerAndMayReturnMilledPermanentToHandEffect;
import com.github.laxika.magicalvibes.model.effect.ReduceCastCostForMatchingSpellsUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.filter.CardIsHistoricPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ACR", collectorNumber = "13")
public class BalladOfTheBlackFlag extends Card {

    public BalladOfTheBlackFlag() {
        CardIsHistoricPredicate historic = new CardIsHistoricPredicate();

        addTrigger(Triggers.chapter(1),
                new MillControllerAndMayReturnMilledPermanentToHandEffect(3, historic));
        addTrigger(Triggers.chapter(2),
                new MillControllerAndMayReturnMilledPermanentToHandEffect(3, historic));
        addTrigger(Triggers.chapter(3),
                new MillControllerAndMayReturnMilledPermanentToHandEffect(3, historic));
        addTrigger(Triggers.chapter(4),
                new ReduceCastCostForMatchingSpellsUntilEndOfTurnEffect(
                        historic, 2));
    }
}
