package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.DealDamageToEnchantedCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "SCG", collectorNumber = "17")
public class GuiltyConscience extends Card {

    public GuiltyConscience() {
        target(TargetFilters.creature())
                .addEffect(EffectSlot.ON_ENCHANTED_CREATURE_DEALS_DAMAGE,
                        new DealDamageToEnchantedCreatureEffect(new EventValue()));
    }
}
