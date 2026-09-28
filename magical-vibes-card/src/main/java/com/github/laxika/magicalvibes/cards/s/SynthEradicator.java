package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.ControllerEnergyAtLeast;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardMayGetEnergyOrPlayThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.PayEnergyCost;

import java.util.List;

@CardRegistration(set = "PIP", collectorNumber = "67")
@CardRegistration(set = "PIP", collectorNumber = "393")
@CardRegistration(set = "PIP", collectorNumber = "595")
@CardRegistration(set = "PIP", collectorNumber = "921")
public class SynthEradicator extends Card {

    public SynthEradicator() {
        addEffect(EffectSlot.ON_ATTACK, new ExileTopCardMayGetEnergyOrPlayThisTurnEffect());

        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new PayEnergyCost(3), new DealDamageToAnyTargetEffect(3)),
                "{T}, Pay {E}{E}{E}: Synth Eradicator deals 3 damage to any target."
        ).withActivationCondition(new ControllerEnergyAtLeast(3),
                "You need at least three energy counters to activate this ability."));
    }
}
