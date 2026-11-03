package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "5ED", collectorNumber = "283")
@CardRegistration(set = "ICE", collectorNumber = "229")
@CardRegistration(set = "ME1", collectorNumber = "116")
public class ChubToad extends Card {

    public ChubToad() {
        // Whenever this creature blocks or becomes blocked, it gets +2/+2 until end of turn.
        addTrigger(Triggers.blocks(), TriggerOptions.ONE_OR_MORE, new BoostSelfEffect(2, 2));
        addTrigger(Triggers.becomesBlocked(), new BoostSelfEffect(2, 2));
    }
}
