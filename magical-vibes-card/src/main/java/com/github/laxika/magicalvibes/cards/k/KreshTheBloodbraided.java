package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEqualToDyingPowerEffect;

@CardRegistration(set = "ALA", collectorNumber = "178")
public class KreshTheBloodbraided extends Card {

    public KreshTheBloodbraided() {
        // Whenever another creature dies, you may put X +1/+1 counters on Kresh, where X is that creature's power.
        addEffect(EffectSlot.ON_ANY_CREATURE_DIES,
                new PutCountersOnSourceEqualToDyingPowerEffect(1, 1, true));
    }
}
