package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "AFR", collectorNumber = "110")
@CardRegistration(set = "HBG", collectorNumber = "161")
public class HoardRobber extends Card {

    public HoardRobber() {
        // Whenever this creature deals combat damage to a player, create a Treasure token.
        addTrigger(Triggers.dealsCombatDamageToPlayer(), CreateTokenEffect.ofTreasureToken(1));
    }
}
