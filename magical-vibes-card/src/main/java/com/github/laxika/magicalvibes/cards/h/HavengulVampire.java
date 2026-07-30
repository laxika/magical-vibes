package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;

@CardRegistration(set = "AVR", collectorNumber = "139")
public class HavengulVampire extends Card {

    public HavengulVampire() {
        // Whenever this creature deals combat damage to a player, put a +1/+1 counter on it.
        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER, new PutCountersOnSourceEffect(1, 1, 1));

        // Whenever another creature dies, put a +1/+1 counter on this creature.
        addEffect(EffectSlot.ON_ANY_CREATURE_DIES, new PutCountersOnSourceEffect(1, 1, 1));
    }
}
