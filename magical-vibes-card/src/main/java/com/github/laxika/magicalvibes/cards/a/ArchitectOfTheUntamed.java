package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.ControllerEnergyAtLeast;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.EnergyCountersEffect;
import com.github.laxika.magicalvibes.model.effect.PayEnergyCost;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "KLD", collectorNumber = "143")
public class ArchitectOfTheUntamed extends Card {

    public ArchitectOfTheUntamed() {
        addEffect(EffectSlot.ON_ALLY_LAND_ENTERS_BATTLEFIELD, new EnergyCountersEffect(1));

        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(
                        new PayEnergyCost(8),
                        new CreateTokenEffect("Beast", 6, 6, null,
                                List.of(CardSubtype.BEAST), Set.of(), Set.of(CardType.ARTIFACT))
                ),
                "Pay eight {E}: Create a 6/6 colorless Beast artifact creature token."
        ).withActivationCondition(new ControllerEnergyAtLeast(8),
                "You need at least eight energy counters to activate this ability."));
    }
}
