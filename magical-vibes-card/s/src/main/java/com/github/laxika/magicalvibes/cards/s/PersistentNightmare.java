package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

public class PersistentNightmare extends Card {

    public PersistentNightmare() {
        addTrigger(Triggers.dealsCombatDamageToPlayer(), ReturnToHandEffect.self());
    }
}
