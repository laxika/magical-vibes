package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "BFZ", collectorNumber = "133")
public class SerpentineSpike extends Card {

    public SerpentineSpike() {
        target(TargetFilters.creature())
                .addEffect(EffectSlot.SPELL,
                        new DealDamageToAnyTargetEffect(new Fixed(2), false, true));
        target(TargetFilters.creature())
                .addEffect(EffectSlot.SPELL,
                        new DealDamageToAnyTargetEffect(new Fixed(3), false, true));
        target(TargetFilters.creature())
                .addEffect(EffectSlot.SPELL,
                        new DealDamageToAnyTargetEffect(new Fixed(4), false, true));
    }
}
