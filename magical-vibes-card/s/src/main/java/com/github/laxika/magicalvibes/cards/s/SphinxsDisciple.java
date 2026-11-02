package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "BNG", collectorNumber = "51")
public class SphinxsDisciple extends Card {

    public SphinxsDisciple() {
        addTrigger(Triggers.becomesUntapped(), new DrawCardEffect(1));
    }
}
