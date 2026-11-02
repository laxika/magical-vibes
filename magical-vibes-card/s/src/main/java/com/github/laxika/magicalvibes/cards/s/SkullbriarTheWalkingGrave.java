package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.PreserveCountersOnZoneChangeEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "2X2", collectorNumber = "277")
@CardRegistration(set = "CMD", collectorNumber = "227")
@CardRegistration(set = "CM2", collectorNumber = "165")
public class SkullbriarTheWalkingGrave extends Card {

    public SkullbriarTheWalkingGrave() {
        addTrigger(Triggers.dealsCombatDamageToPlayer(), new PutCountersOnSourceEffect(1, 1, 1));
        addStaticEffect(new PreserveCountersOnZoneChangeEffect());
    }
}
