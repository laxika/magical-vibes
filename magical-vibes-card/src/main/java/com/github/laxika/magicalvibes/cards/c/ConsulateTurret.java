package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.ControllerEnergyAtLeast;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetPlayerOrPlaneswalkerEffect;
import com.github.laxika.magicalvibes.model.effect.EnergyCountersEffect;
import com.github.laxika.magicalvibes.model.effect.PayEnergyCost;

import java.util.List;

@CardRegistration(set = "AER", collectorNumber = "147")
public class ConsulateTurret extends Card {

    public ConsulateTurret() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new EnergyCountersEffect(1)),
                "{T}: You get {E}."
        ));

        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new PayEnergyCost(3), new DealDamageToTargetPlayerOrPlaneswalkerEffect(2)),
                "{T}, Pay {E}{E}{E}: This artifact deals 2 damage to target player or planeswalker."
        ).withActivationCondition(new ControllerEnergyAtLeast(3),
                "You need at least three energy counters to activate this ability."));
    }
}
