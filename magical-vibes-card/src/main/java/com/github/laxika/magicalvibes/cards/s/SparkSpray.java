package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;

@CardRegistration(set = "SCG", collectorNumber = "105")
@CardRegistration(set = "VMA", collectorNumber = "188")
public class SparkSpray extends Card {

    public SparkSpray() {
        addEffect(EffectSlot.SPELL, new DealDamageToAnyTargetEffect(1));
        addCycling("{R}");
    }
}
