package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "PLC", collectorNumber = "92")
public class VampiricLink extends Card {

    public VampiricLink() {
        target(TargetFilters.creature())
                .addTrigger(Triggers.enchantedCreatureDealsDamage(),
                        new GainLifeEffect(new EventValue()));
    }
}
