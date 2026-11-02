package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.RevealTopCardPutIntoHandAndChangeLifeEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "SHM", collectorNumber = "137")
@CardRegistration(set = "C13", collectorNumber = "227")
public class AuguryAdept extends Card {

    public AuguryAdept() {
        // Whenever this creature deals combat damage to a player, reveal the top card of your
        // library and put that card into your hand. You gain life equal to its mana value.
        addTrigger(Triggers.dealsCombatDamageToPlayer(), new RevealTopCardPutIntoHandAndChangeLifeEffect(true));
    }
}
