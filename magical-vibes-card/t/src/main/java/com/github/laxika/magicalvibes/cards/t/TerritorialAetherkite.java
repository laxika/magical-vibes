package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.EnergyCountersEffect;
import com.github.laxika.magicalvibes.model.effect.PayAnyAmountOfEnergyToDealDamageToEachOtherCreatureEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "DRC", collectorNumber = "12")
@CardRegistration(set = "DRC", collectorNumber = "28")
public class TerritorialAetherkite extends Card {

    public TerritorialAetherkite() {
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                new EnergyCountersEffect(2),
                new PayAnyAmountOfEnergyToDealDamageToEachOtherCreatureEffect());
    }
}
