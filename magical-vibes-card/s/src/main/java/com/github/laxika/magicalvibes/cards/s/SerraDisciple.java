package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.filter.CardIsHistoricPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "DOM", collectorNumber = "34")
public class SerraDisciple extends Card {

    public SerraDisciple() {
        // Whenever you cast a historic spell, Serra Disciple gets +1/+1 until end of turn.
        // (Artifacts, legendaries, and Sagas are historic.)
        addTrigger(Triggers.youCastSpell().withFilter(new CardIsHistoricPredicate()), new BoostSelfEffect(1, 1));
    }
}
