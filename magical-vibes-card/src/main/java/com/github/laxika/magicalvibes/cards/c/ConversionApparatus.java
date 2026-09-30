package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.condition.ControllerEnergyAtLeast;
import com.github.laxika.magicalvibes.model.effect.AwardAnyColorManaEffect;
import com.github.laxika.magicalvibes.model.effect.EnergyCountersEffect;
import com.github.laxika.magicalvibes.model.effect.PayEnergyCost;

import java.util.List;

@CardRegistration(set = "M3C", collectorNumber = "76")
@CardRegistration(set = "M3C", collectorNumber = "128")
public class ConversionApparatus extends Card {

    public ConversionApparatus() {
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.COLORLESS));
        addActivatedAbility(new ActivatedAbility(
                true,
                "{3}",
                List.of(new EnergyCountersEffect(3)),
                "{3}, {T}: You get {E}{E}{E}."
        ));
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new PayEnergyCost(3), new AwardAnyColorManaEffect(3, true)),
                "{T}, Pay {E}{E}{E}: Add three mana in any combination of colors."
        ).withActivationCondition(new ControllerEnergyAtLeast(3),
                "You need at least three energy counters to activate this ability."));
    }
}
