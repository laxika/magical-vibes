package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "PTK", collectorNumber = "145")
@CardRegistration(set = "ME3", collectorNumber = "133")
public class SlashingTiger extends Card {

    public SlashingTiger() {
        // Whenever this creature becomes blocked, it gets +2/+2 until end of turn.
        addTrigger(Triggers.becomesBlocked(), new BoostSelfEffect(2, 2));
    }
}
