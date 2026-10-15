package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.ControllerEnergyAtLeast;
import com.github.laxika.magicalvibes.model.effect.EnergyCountersEffect;
import com.github.laxika.magicalvibes.model.effect.PayEnergyCost;
import com.github.laxika.magicalvibes.model.effect.PreventDamageFromChosenSourceEffect;

import java.util.List;

@CardRegistration(set = "KLD", collectorNumber = "10")
public class ConsulateSurveillance extends Card {

    public ConsulateSurveillance() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new EnergyCountersEffect(4));

        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(new PayEnergyCost(2), PreventDamageFromChosenSourceEffect.allDamageToYou()),
                "Pay {E}{E}: Prevent all damage that would be dealt to you this turn by a source of your choice."
        ).withActivationCondition(new ControllerEnergyAtLeast(2),
                "You need at least two energy counters to activate this ability."));
    }
}
