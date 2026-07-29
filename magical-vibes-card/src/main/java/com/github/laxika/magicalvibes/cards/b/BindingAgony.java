package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.EnchantedCreatureDealsDamageEqualToDealtDamageToControllerEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "MIR", collectorNumber = "106")
public class BindingAgony extends Card {

    public BindingAgony() {
        target(TargetFilters.creature())
                .addEffect(EffectSlot.ON_ENCHANTED_CREATURE_DEALT_DAMAGE,
                        new EnchantedCreatureDealsDamageEqualToDealtDamageToControllerEffect(true));
    }
}
