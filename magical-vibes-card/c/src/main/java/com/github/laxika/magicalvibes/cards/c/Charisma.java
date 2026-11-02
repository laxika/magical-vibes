package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ControlDuration;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MMQ", collectorNumber = "66")
public class Charisma extends Card {

    public Charisma() {
        target(TargetFilters.creature())
                .addTrigger(Triggers.enchantedCreatureDealsDamageToCreature(),
                        new GainControlOfTargetEffect(ControlDuration.WHILE_SOURCE_REMAINS));
    }
}
