package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "8ED", collectorNumber = "253")
public class GiantBadger extends Card {

    public GiantBadger() {
        // Whenever this creature blocks, it gets +2/+2 until end of turn.
        addTrigger(Triggers.blocks(), TriggerOptions.ONE_OR_MORE, new BoostSelfEffect(2, 2));
    }
}
