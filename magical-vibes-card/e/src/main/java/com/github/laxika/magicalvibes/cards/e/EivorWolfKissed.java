package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.MillControllerAndMayPutMilledSagaAndLandOntoBattlefieldEffect;

@CardRegistration(set = "ACR", collectorNumber = "54")
public class EivorWolfKissed extends Card {

    public EivorWolfKissed() {
        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER,
                new MillControllerAndMayPutMilledSagaAndLandOntoBattlefieldEffect(new EventValue()));
    }
}
