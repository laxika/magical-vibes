package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.condition.ControllerEnergyAtLeast;
import com.github.laxika.magicalvibes.model.effect.EnergyCountersEffect;
import com.github.laxika.magicalvibes.model.effect.FlickerEffect;
import com.github.laxika.magicalvibes.model.effect.FlickerScope;
import com.github.laxika.magicalvibes.model.effect.PayEnergyCost;
import com.github.laxika.magicalvibes.model.effect.ReturnTiming;

import java.util.List;

@CardRegistration(set = "AER", collectorNumber = "4")
public class AethergeodeMiner extends Card {

    public AethergeodeMiner() {
        addEffect(EffectSlot.ON_ATTACK, new EnergyCountersEffect(2));

        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(new PayEnergyCost(2), new FlickerEffect(
                        FlickerScope.SELF,
                        null,
                        ReturnTiming.IMMEDIATE,
                        TurnStep.END_STEP,
                        false,
                        null,
                        null,
                        0,
                        false,
                        false
                )),
                "Pay {E}{E}: Exile this creature, then return it to the battlefield under its owner's control."
        ).withActivationCondition(new ControllerEnergyAtLeast(2),
                "You need at least two energy counters to activate this ability."));
    }
}
