package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.ControllerEnergyAtLeast;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.EnergyCountersEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;

@CardRegistration(set = "PIP", collectorNumber = "54")
@CardRegistration(set = "PIP", collectorNumber = "582")
@CardRegistration(set = "PIP", collectorNumber = "384")
@CardRegistration(set = "PIP", collectorNumber = "912")
public class AssaultronDominator extends Card {

    public AssaultronDominator() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new EnergyCountersEffect(2));

        PermanentAllOfPredicate artifactCreature = new PermanentAllOfPredicate(List.of(
                new PermanentIsArtifactPredicate(),
                new PermanentIsCreaturePredicate()));
        ChooseOneEffect counterChoice = new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Put a +1/+1 counter on that creature",
                        new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE)),
                new ChooseOneEffect.ChooseOneOption(
                        "Put a first strike counter on that creature",
                        new PutCountersOnSelfEffect(CounterType.FIRST_STRIKE)),
                new ChooseOneEffect.ChooseOneOption(
                        "Put a trample counter on that creature",
                        new PutCountersOnSelfEffect(CounterType.TRAMPLE))));

        addEffect(EffectSlot.ON_ALLY_CREATURE_ATTACKS,
                new TriggeringPermanentConditionalEffect(artifactCreature,
                        new MayEffect(
                                ConditionalEffect.unless(new ControllerEnergyAtLeast(1),
                                        SequenceEffect.of(new EnergyCountersEffect(-1), counterChoice)),
                                "Pay {E} to put your choice of a +1/+1, first strike, or trample counter on that creature?")));
    }
}
