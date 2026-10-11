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
        DealDamageToAnyTargetEffect firstDamage = new DealDamageToAnyTargetEffect(new Fixed(2), false, true);
        DealDamageToAnyTargetEffect secondDamage = new DealDamageToAnyTargetEffect(new Fixed(3), false, true);
        DealDamageToAnyTargetEffect thirdDamage = new DealDamageToAnyTargetEffect(new Fixed(4), false, true);
        target(TargetFilters.creature()).bindEffect(firstDamage);
        target(TargetFilters.creature()).bindEffect(secondDamage);
        target(TargetFilters.creature()).bindEffect(thirdDamage);
        addSimultaneousEffects(EffectSlot.SPELL, firstDamage, secondDamage, thirdDamage);
    }
}
