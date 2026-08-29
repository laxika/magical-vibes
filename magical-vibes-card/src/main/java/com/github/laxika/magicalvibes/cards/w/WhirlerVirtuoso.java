package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
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

@CardRegistration(set = "KLD", collectorNumber = "190")
public class WhirlerVirtuoso extends Card {

    public WhirlerVirtuoso() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new EnergyCountersEffect(3));

        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(
                        new PayEnergyCost(3),
                        new CreateTokenEffect("Thopter", 1, 1, null,
                                List.of(CardSubtype.THOPTER), Set.of(Keyword.FLYING), Set.of(CardType.ARTIFACT))
                ),
                "Pay {E}{E}{E}: Create a 1/1 colorless Thopter artifact creature token with flying."
        ).withActivationCondition(new ControllerEnergyAtLeast(3),
                "You need at least three energy counters to activate this ability."));
    }
}
