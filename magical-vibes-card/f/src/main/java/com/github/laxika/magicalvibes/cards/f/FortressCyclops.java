package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.TriggerMode;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "GTC", collectorNumber = "164")
public class FortressCyclops extends Card {

    public FortressCyclops() {
        // Whenever this creature attacks, it gets +3/+0 until end of turn.
        addTrigger(Triggers.attacks(), new BoostSelfEffect(3, 0));

        // Whenever this creature blocks, it gets +0/+3 until end of turn.
        addTrigger(Triggers.blocks(), TriggerMode.ONCE_PER_BLOCK, new BoostSelfEffect(0, 3));
    }
}
