package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DestroySelfAtEndOfCombatEffect;

@CardRegistration(set = "MMQ", collectorNumber = "182")
public class CeremonialGuard extends Card {

    public CeremonialGuard() {
        // When this creature attacks or blocks, destroy it at end of combat.
        addEffect(EffectSlot.ON_ATTACK, new DestroySelfAtEndOfCombatEffect());
        addEffect(EffectSlot.ON_BLOCK, new DestroySelfAtEndOfCombatEffect());
    }
}
