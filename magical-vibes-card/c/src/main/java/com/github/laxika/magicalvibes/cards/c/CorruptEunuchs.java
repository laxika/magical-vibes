package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "PTK", collectorNumber = "106")
@CardRegistration(set = "ME3", collectorNumber = "92")
public class CorruptEunuchs extends Card {

    public CorruptEunuchs() {
        // When this creature enters, it deals 2 damage to target creature.
        target(TargetFilters.creature()).addTrigger(Triggers.enters(ObjectRelation.THIS),
                new DealDamageToTargetCreatureEffect(2));
    }
}
