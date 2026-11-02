package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.MadnessCast;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "INR", collectorNumber = "145")
@CardRegistration(set = "SOI", collectorNumber = "146")
@CardRegistration(set = "SIR", collectorNumber = "145")
public class BloodmadVampire extends Card {

    public BloodmadVampire() {
        // Madness {1}{R}
        addCastingOption(new MadnessCast("{1}{R}"));

        // Whenever this creature deals combat damage to a player, put a +1/+1 counter on it.
        addTrigger(Triggers.dealsCombatDamageToPlayer(), new PutCountersOnSourceEffect(1, 1, 1));
    }
}
