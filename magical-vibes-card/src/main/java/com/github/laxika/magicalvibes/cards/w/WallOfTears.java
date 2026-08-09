package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ReturnCombatOpponentToHandAtEndOfCombatEffect;

@CardRegistration(set = "STH", collectorNumber = "50")
public class WallOfTears extends Card {

    public WallOfTears() {
        // Whenever this creature blocks a creature, return that creature to its owner's hand at
        // end of combat.
        addEffect(EffectSlot.ON_BLOCK, new ReturnCombatOpponentToHandAtEndOfCombatEffect());
    }
}
