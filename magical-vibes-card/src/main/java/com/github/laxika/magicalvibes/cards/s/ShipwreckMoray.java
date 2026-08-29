package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.ControllerEnergyAtLeast;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.EnergyCountersEffect;
import com.github.laxika.magicalvibes.model.effect.PayEnergyCost;

import java.util.List;

@CardRegistration(set = "AER", collectorNumber = "45")
public class ShipwreckMoray extends Card {

    public ShipwreckMoray() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new EnergyCountersEffect(4));

        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(new PayEnergyCost(1), new BoostSelfEffect(2, -2)),
                "Pay {E}: This creature gets +2/-2 until end of turn."
        ).withActivationCondition(new ControllerEnergyAtLeast(1),
                "You need at least one energy counter to activate this ability."));
    }
}
