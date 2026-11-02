package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.MillControllerAndMayPutMilledSagaAndLandOntoBattlefieldEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ACR", collectorNumber = "54")
public class EivorWolfKissed extends Card {

    public EivorWolfKissed() {
        addTrigger(Triggers.dealsCombatDamageToPlayer(),
                new MillControllerAndMayPutMilledSagaAndLandOntoBattlefieldEffect(new EventValue()));
    }
}
