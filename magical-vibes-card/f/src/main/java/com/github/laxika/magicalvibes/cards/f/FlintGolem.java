package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.MillDefendingPlayerEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "NEM", collectorNumber = "130")
public class FlintGolem extends Card {

    public FlintGolem() {
        addTrigger(Triggers.becomesBlocked(), new MillDefendingPlayerEffect(3));
    }
}
