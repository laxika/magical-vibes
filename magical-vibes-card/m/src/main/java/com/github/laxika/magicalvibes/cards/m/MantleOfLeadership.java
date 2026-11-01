package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.BoostEquippedCreatureUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "PLC", collectorNumber = "9")
public class MantleOfLeadership extends Card {

    public MantleOfLeadership() {
        // Whenever a creature enters, enchanted creature gets +2/+2 until end of turn.
        target(TargetFilters.creature())
                .addTrigger(Triggers.enters(Triggers.CREATURE, ObjectRelation.ANOTHER),
                        new BoostEquippedCreatureUntilEndOfTurnEffect(new Fixed(2), new Fixed(2)));
    }
}
