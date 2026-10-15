package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;

@CardRegistration(set = "HML", collectorNumber = "57a")
@CardRegistration(set = "HML", collectorNumber = "57b")
public class SengirBats extends Card {

    public SengirBats() {
        addEffect(EffectSlot.ON_DAMAGED_CREATURE_DIES, new PutCountersOnSourceEffect(1, 1, 1));
    }
}
