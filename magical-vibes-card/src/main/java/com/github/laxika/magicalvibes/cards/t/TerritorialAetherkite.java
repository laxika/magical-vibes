package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.EnergyCountersEffect;
import com.github.laxika.magicalvibes.model.effect.PayAnyAmountOfEnergyToDealDamageToEachOtherCreatureEffect;

@CardRegistration(set = "DRC", collectorNumber = "12")
@CardRegistration(set = "DRC", collectorNumber = "28")
public class TerritorialAetherkite extends Card {

    public TerritorialAetherkite() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new EnergyCountersEffect(2));
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new PayAnyAmountOfEnergyToDealDamageToEachOtherCreatureEffect());
    }
}
