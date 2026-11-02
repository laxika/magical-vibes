package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

public class BearerOfOverwhelmingTruths extends Card {

    public BearerOfOverwhelmingTruths() {
        // Whenever this creature deals combat damage to a player, investigate.
        addTrigger(Triggers.dealsCombatDamageToPlayer(), CreateTokenEffect.ofClueToken(1));
    }
}
