package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.RampageEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ALL", collectorNumber = "66a")
@CardRegistration(set = "ALL", collectorNumber = "66b")
public class BalduvianWarMakers extends Card {

    public BalduvianWarMakers() {
        // Rampage 1: whenever this creature becomes blocked, it gets +1/+1 until end of
        // turn for each creature blocking it beyond the first, i.e. blockers - 1.
        addTrigger(Triggers.becomesBlocked(), new RampageEffect(1));
    }
}
