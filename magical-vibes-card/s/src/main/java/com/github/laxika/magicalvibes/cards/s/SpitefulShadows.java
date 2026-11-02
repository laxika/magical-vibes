package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.EnchantedCreatureDealsDamageEqualToDealtDamageToControllerEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "DKA", collectorNumber = "75")
public class SpitefulShadows extends Card {

    public SpitefulShadows() {
        target(TargetFilters.creature())
                .addTrigger(Triggers.enchantedCreatureDealtDamage(),
                        new EnchantedCreatureDealsDamageEqualToDealtDamageToControllerEffect());
    }
}
