package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;

@CardRegistration(set = "AA4", collectorNumber = "20")
public class MagmaticForce extends Card {

    public MagmaticForce() {
        addEffect(EffectSlot.EACH_UPKEEP_TRIGGERED, new DealDamageToAnyTargetEffect(3));
    }
}
