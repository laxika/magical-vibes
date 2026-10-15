package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;

@CardRegistration(set = "40K", collectorNumber = "53")
public class SanguinaryPriest extends Card {

    public SanguinaryPriest() {
        addEffect(EffectSlot.ON_ALLY_CREATURE_DIES, new DealDamageToAnyTargetEffect(1));
    }
}
