package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.ControllerEnergyAtLeast;
import com.github.laxika.magicalvibes.model.effect.EnergyCountersEffect;
import com.github.laxika.magicalvibes.model.effect.PayEnergyCost;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;

import java.util.List;

@CardRegistration(set = "KLD", collectorNumber = "161")
public class LongtuskCub extends Card {

    public LongtuskCub() {
        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER, new EnergyCountersEffect(2));

        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(
                        new PayEnergyCost(2),
                        new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE)
                ),
                "Pay {E}{E}: Put a +1/+1 counter on this creature."
        ).withActivationCondition(new ControllerEnergyAtLeast(2),
                "You need at least two energy counters to activate this ability."));
    }
}
