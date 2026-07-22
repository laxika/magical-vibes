package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.MadnessCast;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;

@CardRegistration(set = "INR", collectorNumber = "145")
public class BloodmadVampire extends Card {

    public BloodmadVampire() {
        // Madness {1}{R}
        addCastingOption(new MadnessCast("{1}{R}"));

        // Whenever this creature deals combat damage to a player, put a +1/+1 counter on it.
        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER, new PutCountersOnSourceEffect(1, 1, 1));
    }
}
