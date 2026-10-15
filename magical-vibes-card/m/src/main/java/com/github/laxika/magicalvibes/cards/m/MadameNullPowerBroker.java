package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MayPayLifeAndPutCountersOnEnteringCreatureEqualToPowerEffect;

@CardRegistration(set = "TMT", collectorNumber = "66")
@CardRegistration(set = "TMT", collectorNumber = "265")
public class MadameNullPowerBroker extends Card {

    public MadameNullPowerBroker() {
        addEffect(EffectSlot.ON_ALLY_CREATURE_ENTERS_BATTLEFIELD,
                new MayPayLifeAndPutCountersOnEnteringCreatureEqualToPowerEffect());
    }
}
