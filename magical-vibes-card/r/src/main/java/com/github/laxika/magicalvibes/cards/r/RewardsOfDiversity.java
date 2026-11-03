package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.filter.CardIsMulticoloredPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "INV", collectorNumber = "32")
public class RewardsOfDiversity extends Card {

    public RewardsOfDiversity() {
        addTrigger(Triggers.opponentCastsSpell().withFilter(new CardIsMulticoloredPredicate()), new GainLifeEffect(4));
    }
}
