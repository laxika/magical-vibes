package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.EnteringCreatureExactStatsConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnEnteringCreatureEffect;

@CardRegistration(set = "ARB", collectorNumber = "77")
public class SigilCaptain extends Card {

    public SigilCaptain() {
        // Whenever a creature you control enters, if that creature is 1/1, put two +1/+1 counters on it.
        addEffect(EffectSlot.ON_ALLY_CREATURE_ENTERS_BATTLEFIELD,
                new EnteringCreatureExactStatsConditionalEffect(1, 1,
                        new PutCountersOnEnteringCreatureEffect(2, false)));
    }
}
