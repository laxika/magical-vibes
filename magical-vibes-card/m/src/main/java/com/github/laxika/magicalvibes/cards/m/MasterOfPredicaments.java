package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.MasterOfPredicamentsEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "M15", collectorNumber = "67")
public class MasterOfPredicaments extends Card {

    public MasterOfPredicaments() {
        addTrigger(Triggers.dealsCombatDamageToPlayer(), new MasterOfPredicamentsEffect());
    }
}
