package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.amount.GreatestPowerAmongControlled;
import com.github.laxika.magicalvibes.model.condition.ControllerEnergyAtLeast;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.EnergyCountersEffect;
import com.github.laxika.magicalvibes.model.effect.PayEnergyCost;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;

import java.util.List;

@CardRegistration(set = "DRC", collectorNumber = "14")
@CardRegistration(set = "DRC", collectorNumber = "30")
public class PeemaTrailblazer extends Card {

    public PeemaTrailblazer() {
        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER,
                new EnergyCountersEffect(new EventValue()));

        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(
                        new PayEnergyCost(6),
                        new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE, 2),
                        new DrawCardEffect(new GreatestPowerAmongControlled())
                ),
                "Exhaust — Pay six {E}: Put two +1/+1 counters on this creature. Then draw cards "
                        + "equal to the greatest power among creatures you control."
        ).withActivationCondition(new ControllerEnergyAtLeast(6),
                "You need at least six energy counters to activate this ability.")
                .withMaxActivationsPerGame(1)
                .withExhaust());
    }
}
