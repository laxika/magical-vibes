package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "VIS", collectorNumber = "90")
public class RagingGorilla extends Card {

    public RagingGorilla() {
        // Whenever this creature blocks or becomes blocked, it gets +2/-2 until end of turn.
        addTrigger(Triggers.blocks(), TriggerOptions.ONE_OR_MORE, new BoostSelfEffect(2, -2));
        addTrigger(Triggers.becomesBlocked(), new BoostSelfEffect(2, -2));
    }
}
