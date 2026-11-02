package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "RTR", collectorNumber = "53")
@CardRegistration(set = "DDM", collectorNumber = "11")
@CardRegistration(set = "W17", collectorNumber = "13")
public class StealerOfSecrets extends Card {

    public StealerOfSecrets() {
        // Whenever this creature deals combat damage to a player, draw a card.
        addTrigger(Triggers.dealsCombatDamageToPlayer(), new DrawCardEffect(1));
    }
}
