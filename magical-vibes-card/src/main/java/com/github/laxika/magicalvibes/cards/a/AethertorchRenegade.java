package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.ControllerEnergyAtLeast;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetPlayerOrPlaneswalkerEffect;
import com.github.laxika.magicalvibes.model.effect.EnergyCountersEffect;
import com.github.laxika.magicalvibes.model.effect.PayEnergyCost;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "KLD", collectorNumber = "106")
public class AethertorchRenegade extends Card {

    public AethertorchRenegade() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new EnergyCountersEffect(4));

        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new PayEnergyCost(2), new DealDamageToTargetCreatureEffect(1)),
                "{T}, Pay {E}{E}: This creature deals 1 damage to target creature.",
                TargetFilters.creature()
        ).withActivationCondition(new ControllerEnergyAtLeast(2),
                "You need at least two energy counters to activate this ability."));

        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new PayEnergyCost(8), new DealDamageToTargetPlayerOrPlaneswalkerEffect(6)),
                "{T}, Pay eight {E}: This creature deals 6 damage to target player or planeswalker."
        ).withActivationCondition(new ControllerEnergyAtLeast(8),
                "You need at least eight energy counters to activate this ability."));
    }
}
