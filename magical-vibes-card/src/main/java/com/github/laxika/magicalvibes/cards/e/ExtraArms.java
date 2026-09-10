package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "SCG", collectorNumber = "92")
public class ExtraArms extends Card {

    public ExtraArms() {
        // Enchant creature
        target(TargetFilters.creature());

        // Whenever enchanted creature attacks, it deals 2 damage to any target.
        addEffect(EffectSlot.ON_ATTACK, new DealDamageToAnyTargetEffect(2));
    }
}
