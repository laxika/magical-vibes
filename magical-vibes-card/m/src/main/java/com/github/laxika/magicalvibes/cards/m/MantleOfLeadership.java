package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.BoostEquippedCreatureUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "PLC", collectorNumber = "9")
public class MantleOfLeadership extends Card {

    public MantleOfLeadership() {
        // Whenever a creature enters, enchanted creature gets +2/+2 until end of turn.
        target(TargetFilters.creature())
                .addEffect(EffectSlot.ON_ANY_OTHER_CREATURE_ENTERS_BATTLEFIELD,
                        new BoostEquippedCreatureUntilEndOfTurnEffect(new Fixed(2), new Fixed(2)));
    }
}
