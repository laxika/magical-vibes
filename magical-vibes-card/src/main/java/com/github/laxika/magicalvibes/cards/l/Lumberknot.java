package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;

@CardRegistration(set = "ISD", collectorNumber = "191")
public class Lumberknot extends Card {

    public Lumberknot() {
        // Whenever a creature dies, put a +1/+1 counter on Lumberknot.
        addEffect(EffectSlot.ON_ANY_CREATURE_DIES, new PutCountersOnSourceEffect(1, 1, 1));
    }
}
