package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "PLS", collectorNumber = "63")
public class Insolence extends Card {

    public Insolence() {
        target(TargetFilters.creature()).addTrigger(Triggers.enchantedPermanentBecomesTapped(),
                DealDamageEffect.to(Subjects.controllerOf(Subjects.triggering()), 2));
    }
}
