package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.TriggerMode;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "USG", collectorNumber = "182")
public class Dromosaur extends Card {

    public Dromosaur() {
        // Whenever this creature blocks or becomes blocked, it gets +2/-2 until end of turn.
        addTrigger(Triggers.blocks(), TriggerMode.ONCE_PER_BLOCK, new BoostSelfEffect(2, -2));
        addTrigger(Triggers.becomesBlocked(), new BoostSelfEffect(2, -2));
    }
}
