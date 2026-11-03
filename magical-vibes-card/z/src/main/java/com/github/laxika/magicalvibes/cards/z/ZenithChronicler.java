package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardsForEachOpponentOfTriggeringPlayerEffect;
import com.github.laxika.magicalvibes.model.filter.CardIsMulticoloredPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ONE", collectorNumber = "246")
public class ZenithChronicler extends Card {

    public ZenithChronicler() {
        addTrigger(Triggers.playerCastsSpell().withFilter(new CardIsMulticoloredPredicate()).nthThisTurn(1),
                new DrawCardsForEachOpponentOfTriggeringPlayerEffect(1));
    }
}
