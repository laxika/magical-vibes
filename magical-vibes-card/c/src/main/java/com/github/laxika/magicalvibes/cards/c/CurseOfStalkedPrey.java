package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ISD", collectorNumber = "136")
public class CurseOfStalkedPrey extends Card {

    public CurseOfStalkedPrey() {
        addTrigger(Triggers.dealsCombatDamageToPlayer(),
                new PutCounterOnTargetPermanentEffect(Subjects.triggering(),
                        CounterType.PLUS_ONE_PLUS_ONE));
    }
}
