package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfSourceEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "SHM", collectorNumber = "129")
@CardRegistration(set = "DDM", collectorNumber = "55")
@CardRegistration(set = "CMD", collectorNumber = "171")
public class Spawnwrithe extends Card {

    public Spawnwrithe() {
        // Trample (keyword auto-loaded from oracle data).
        // Whenever this creature deals combat damage to a player, create a token
        // that's a copy of this creature (the copy has the same trigger).
        addTrigger(Triggers.dealsCombatDamageToPlayer(), new CreateTokenCopyOfSourceEffect());
    }
}
