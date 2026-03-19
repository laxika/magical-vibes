package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;

@CardRegistration(set = "ISD", collectorNumber = "39")
public class UnrulyMob extends Card {

    public UnrulyMob() {
        // Whenever another creature you control dies, put a +1/+1 counter on Unruly Mob.
        addEffect(EffectSlot.ON_ALLY_CREATURE_DIES, new PutCountersOnSourceEffect(1, 1, 1));
    }
}
