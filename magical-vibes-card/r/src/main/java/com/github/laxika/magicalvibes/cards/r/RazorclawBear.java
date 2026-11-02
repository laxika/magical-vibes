package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "P02", collectorNumber = "142")
public class RazorclawBear extends Card {

    public RazorclawBear() {
        // Whenever this creature becomes blocked, it gets +2/+2 until end of turn.
        addTrigger(Triggers.becomesBlocked(), new BoostSelfEffect(2, 2));
    }
}
