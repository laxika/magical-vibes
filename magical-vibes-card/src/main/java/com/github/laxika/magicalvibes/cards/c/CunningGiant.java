package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AssignCombatDamageToDefendingCreatureWhenUnblockedEffect;

@CardRegistration(set = "P02", collectorNumber = "93")
public class CunningGiant extends Card {

    public CunningGiant() {
        // If this creature is unblocked, you may have it assign its combat damage
        // to a creature defending player controls.
        addEffect(EffectSlot.STATIC, new AssignCombatDamageToDefendingCreatureWhenUnblockedEffect());
    }
}
