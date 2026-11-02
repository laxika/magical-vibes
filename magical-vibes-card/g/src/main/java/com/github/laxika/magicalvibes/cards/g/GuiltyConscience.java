package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.DealDamageToEnchantedCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "SCG", collectorNumber = "17")
public class GuiltyConscience extends Card {

    public GuiltyConscience() {
        target(TargetFilters.creature())
                .addTrigger(Triggers.enchantedCreatureDealsDamage(),
                        new DealDamageToEnchantedCreatureEffect(new EventValue()));
    }
}
