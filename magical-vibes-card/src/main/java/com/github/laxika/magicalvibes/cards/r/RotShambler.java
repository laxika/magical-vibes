package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;

@CardRegistration(set = "BFZ", collectorNumber = "187")
public class RotShambler extends Card {

    public RotShambler() {
        addEffect(EffectSlot.ON_ALLY_CREATURE_DIES, new PutCountersOnSourceEffect(1, 1, 1));
    }
}
