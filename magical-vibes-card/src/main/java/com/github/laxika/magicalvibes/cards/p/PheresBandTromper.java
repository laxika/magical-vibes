package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;

@CardRegistration(set = "BNG", collectorNumber = "134")
public class PheresBandTromper extends Card {

    public PheresBandTromper() {
        addEffect(EffectSlot.ON_SELF_BECOMES_UNTAPPED, new PutCountersOnSourceEffect(1, 1, 1));
    }
}
