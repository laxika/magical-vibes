package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.EnchantedCreatureDealsDamageEqualToDealtDamageToControllerEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MIR", collectorNumber = "106")
public class BindingAgony extends Card {

    public BindingAgony() {
        target(TargetFilters.creature())
                .addTrigger(Triggers.enchantedCreatureDealtDamage(),
                        new EnchantedCreatureDealsDamageEqualToDealtDamageToControllerEffect(true));
    }
}
