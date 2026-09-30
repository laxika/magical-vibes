package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.ControllerEnergyAtLeast;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.EnergyCountersEffect;
import com.github.laxika.magicalvibes.model.effect.PayEnergyCost;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "M3C", collectorNumber = "77")
@CardRegistration(set = "M3C", collectorNumber = "129")
public class StoneIdolGenerator extends Card {

    public StoneIdolGenerator() {
        addEffect(EffectSlot.ON_ALLY_CREATURE_ATTACKS, new EnergyCountersEffect(1));

        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(
                        new PayEnergyCost(6),
                        new CreateTokenEffect("Construct", 6, 12, null,
                                List.of(CardSubtype.CONSTRUCT), Set.of(Keyword.TRAMPLE), Set.of(CardType.ARTIFACT))
                ),
                "Pay six {E}: Create a 6/12 colorless Construct artifact creature token with trample.",
                ActivationTimingRestriction.SORCERY_SPEED
        ).withActivationCondition(new ControllerEnergyAtLeast(6),
                "You need at least six energy counters to activate this ability."));
    }
}
