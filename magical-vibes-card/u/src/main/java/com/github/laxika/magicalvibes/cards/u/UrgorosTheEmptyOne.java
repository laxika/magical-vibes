package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerRandomDiscardOrControllerDrawsEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "DOM", collectorNumber = "109")
public class UrgorosTheEmptyOne extends Card {

    public UrgorosTheEmptyOne() {
        addTrigger(Triggers.dealsCombatDamageToPlayer(), new TargetPlayerRandomDiscardOrControllerDrawsEffect());
    }
}
