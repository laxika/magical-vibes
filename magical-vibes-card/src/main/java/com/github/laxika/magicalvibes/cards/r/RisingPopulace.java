package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;

@CardRegistration(set = "WAR", collectorNumber = "29")
public class RisingPopulace extends Card {

    public RisingPopulace() {
        // Whenever another creature or planeswalker you control dies, put a +1/+1 counter on this creature.
        addEffect(EffectSlot.ON_ALLY_CREATURE_OR_PLANESWALKER_DIES,
                new PutCountersOnSourceEffect(1, 1, 1));
    }
}
