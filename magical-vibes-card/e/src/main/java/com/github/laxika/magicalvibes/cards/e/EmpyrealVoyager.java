package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.EnergyCountersEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "KLD", collectorNumber = "180")
@CardRegistration(set = "KLR", collectorNumber = "194")
public class EmpyrealVoyager extends Card {

    public EmpyrealVoyager() {
        addTrigger(Triggers.dealsCombatDamageToPlayer(), new EnergyCountersEffect(new EventValue()));
    }
}
