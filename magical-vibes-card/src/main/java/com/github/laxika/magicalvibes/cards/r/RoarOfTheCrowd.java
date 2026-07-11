package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEqualToChosenTypeCountEffect;

@CardRegistration(set = "MOR", collectorNumber = "100")
public class RoarOfTheCrowd extends Card {

    public RoarOfTheCrowd() {
        addEffect(EffectSlot.SPELL, new DealDamageToAnyTargetEqualToChosenTypeCountEffect());
    }
}
