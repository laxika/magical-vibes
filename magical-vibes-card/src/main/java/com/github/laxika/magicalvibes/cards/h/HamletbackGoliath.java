package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEqualToEnteringPowerEffect;

@CardRegistration(set = "LRW", collectorNumber = "173")
public class HamletbackGoliath extends Card {

    public HamletbackGoliath() {
        // Whenever another creature enters, you may put X +1/+1 counters on this creature, where X is that creature's power.
        addEffect(EffectSlot.ON_ANY_OTHER_CREATURE_ENTERS_BATTLEFIELD,
                new PutCountersOnSourceEqualToEnteringPowerEffect(1, 1, true));
    }
}
