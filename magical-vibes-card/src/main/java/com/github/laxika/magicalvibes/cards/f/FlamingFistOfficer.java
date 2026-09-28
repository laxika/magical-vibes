package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;

@CardRegistration(set = "HBG", collectorNumber = "89")
public class FlamingFistOfficer extends Card {

    public FlamingFistOfficer() {
        // Whenever another creature you control leaves the battlefield, put a +1/+1 counter on this creature.
        addEffect(EffectSlot.ON_ALLY_CREATURE_LEAVES_BATTLEFIELD,
                new PutCountersOnSourceEffect(1, 1, 1));
    }
}
