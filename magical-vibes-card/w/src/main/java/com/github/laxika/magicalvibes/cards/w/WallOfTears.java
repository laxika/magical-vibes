package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ReturnCombatOpponentToHandAtEndOfCombatEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "STH", collectorNumber = "50")
public class WallOfTears extends Card {

    public WallOfTears() {
        // Whenever this creature blocks a creature, return that creature to its owner's hand at
        // end of combat.
        addTrigger(Triggers.blocks(), new ReturnCombatOpponentToHandAtEndOfCombatEffect());
    }
}
