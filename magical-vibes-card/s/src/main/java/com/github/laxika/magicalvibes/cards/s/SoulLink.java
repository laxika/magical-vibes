package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "APC", collectorNumber = "120")
public class SoulLink extends Card {

    public SoulLink() {
        target(TargetFilters.creature())
                .addTrigger(Triggers.enchantedCreatureDealsDamage(),
                        new GainLifeEffect(new EventValue()))
                .addTrigger(Triggers.enchantedCreatureDealtDamage(),
                        new GainLifeEffect(new EventValue()));
    }
}
