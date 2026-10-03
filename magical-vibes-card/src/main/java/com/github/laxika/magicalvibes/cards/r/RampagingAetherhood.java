package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.SourcePower;
import com.github.laxika.magicalvibes.model.effect.EnergyCountersEffect;
import com.github.laxika.magicalvibes.model.effect.PayAnyAmountOfEnergyToPutCountersOnSelfEffect;

@CardRegistration(set = "DRC", collectorNumber = "15")
@CardRegistration(set = "DRC", collectorNumber = "31")
public class RampagingAetherhood extends Card {

    public RampagingAetherhood() {
        addEffect(EffectSlot.UPKEEP_TRIGGERED, new EnergyCountersEffect(new SourcePower()));
        addEffect(EffectSlot.UPKEEP_TRIGGERED,
                new PayAnyAmountOfEnergyToPutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE));
    }
}
