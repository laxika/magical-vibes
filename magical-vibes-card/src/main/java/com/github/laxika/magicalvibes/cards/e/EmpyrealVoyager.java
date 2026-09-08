package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.EnergyCountersEffect;

@CardRegistration(set = "KLD", collectorNumber = "180")
public class EmpyrealVoyager extends Card {

    public EmpyrealVoyager() {
        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER, new EnergyCountersEffect(new EventValue()));
    }
}
