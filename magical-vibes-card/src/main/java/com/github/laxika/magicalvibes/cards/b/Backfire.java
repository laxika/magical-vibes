package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.EnchantedCreatureDealsDamageEqualToDealtDamageToControllerEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "4ED", collectorNumber = "62")
public class Backfire extends Card {

    public Backfire() {
        target(TargetFilters.creature())
                // Whenever enchanted creature deals damage to you, this Aura deals that much
                // damage to that creature's controller.
                .addEffect(EffectSlot.ON_ENCHANTED_CREATURE_DEALS_DAMAGE_TO_YOU,
                        new EnchantedCreatureDealsDamageEqualToDealtDamageToControllerEffect());
    }
}
