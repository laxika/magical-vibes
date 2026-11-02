package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.EachPlayerDrawsCardEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "OPC2", collectorNumber = "4")
@CardRegistration(set = "OPCA", collectorNumber = "4")
@CardRegistration(set = "MOC", collectorNumber = "151")
public class MutualEpiphany extends Card {

    public MutualEpiphany() {
        addTrigger(Triggers.encounterThis(), new EachPlayerDrawsCardEffect(4));
    }
}
