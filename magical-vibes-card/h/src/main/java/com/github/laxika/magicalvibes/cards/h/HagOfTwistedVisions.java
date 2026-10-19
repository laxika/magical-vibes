package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;

@CardRegistration(set = "HBG", collectorNumber = "H25")
public class HagOfTwistedVisions extends Card {

    public HagOfTwistedVisions() {
        // At the beginning of your upkeep, scry 2.
        addEffect(EffectSlot.UPKEEP_TRIGGERED, new ScryEffect(2));
    }
}
