package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;

@CardRegistration(set = "CSP", collectorNumber = "59")
public class GristleGrinner extends Card {

    public GristleGrinner() {
        addEffect(EffectSlot.ON_ANY_CREATURE_DIES, new BoostSelfEffect(2, 2));
    }
}
