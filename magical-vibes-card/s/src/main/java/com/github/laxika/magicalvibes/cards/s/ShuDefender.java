package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.TriggerMode;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "PTK", collectorNumber = "20")
public class ShuDefender extends Card {

    public ShuDefender() {
        addTrigger(Triggers.blocks(), TriggerMode.ONCE_PER_BLOCK, new BoostSelfEffect(0, 2));
    }
}
