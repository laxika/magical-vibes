package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DestroySelfAtEndOfCombatEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MMQ", collectorNumber = "182")
public class CeremonialGuard extends Card {

    public CeremonialGuard() {
        // When this creature attacks or blocks, destroy it at end of combat.
        addTrigger(Triggers.attacks(), new DestroySelfAtEndOfCombatEffect());
        addTrigger(Triggers.blocks(), new DestroySelfAtEndOfCombatEffect());
    }
}
