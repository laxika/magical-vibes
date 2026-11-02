package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ORI", collectorNumber = "62")
@CardRegistration(set = "IMA", collectorNumber = "61")
@CardRegistration(set = "PIO", collectorNumber = "61")
public class JhessianThief extends Card {

    public JhessianThief() {
        // Prowess is loaded from Scryfall keywords.
        // Whenever this creature deals combat damage to a player, draw a card.
        addTrigger(Triggers.dealsCombatDamageToPlayer(), new DrawCardEffect(1));
    }
}
