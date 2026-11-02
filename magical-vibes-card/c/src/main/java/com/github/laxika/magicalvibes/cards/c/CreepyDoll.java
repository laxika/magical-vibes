package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.FlipCoinWinEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ISD", collectorNumber = "220")
public class CreepyDoll extends Card {

    public CreepyDoll() {
        addTrigger(Triggers.dealsCombatDamageToCreature(), new FlipCoinWinEffect(new DestroyTargetPermanentEffect()));
    }
}
