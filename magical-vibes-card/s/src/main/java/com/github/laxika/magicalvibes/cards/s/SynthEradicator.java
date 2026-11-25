package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.condition.ControllerEnergyAtLeast;
import com.github.laxika.magicalvibes.model.effect.AllowPlayCardsFromExileEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.EnergyCountersEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardOfOwnLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PayEnergyCost;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "PIP", collectorNumber = "67")
@CardRegistration(set = "PIP", collectorNumber = "393")
@CardRegistration(set = "PIP", collectorNumber = "595")
@CardRegistration(set = "PIP", collectorNumber = "921")
public class SynthEradicator extends Card {

    public SynthEradicator() {
        addTrigger(Triggers.attacks(),
                ExileTopCardOfOwnLibraryEffect.of(Subjects.you()),
                new MayEffect(new EnergyCountersEffect(2), "Get {E}{E}?",
                        AllowPlayCardsFromExileEffect.thisTurn(Subjects.cardsThisWay(StepKind.EXILE))));

        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new PayEnergyCost(3), DealDamageEffect.toAnyTarget(3)),
                "{T}, Pay {E}{E}{E}: Synth Eradicator deals 3 damage to any target."
        ).withActivationCondition(new ControllerEnergyAtLeast(3),
                "You need at least three energy counters to activate this ability."));
    }
}
