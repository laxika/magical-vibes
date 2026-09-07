package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AddManaEqualToEnchantedPermanentManaCostEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "DIS", collectorNumber = "83")
public class ElementalResonance extends Card {

    public ElementalResonance() {
        target(TargetFilters.permanent())
                .addEffect(EffectSlot.PRECOMBAT_MAIN_TRIGGERED,
                        new AddManaEqualToEnchantedPermanentManaCostEffect());
    }
}
