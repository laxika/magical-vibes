package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveOneOrMoreCountersFromControlledPermanentsCost;
import com.github.laxika.magicalvibes.model.filter.ControlledPermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;

@CardRegistration(set = "EOC", collectorNumber = "17")
@CardRegistration(set = "EOC", collectorNumber = "37")
public class MoxiteRefinery extends Card {

    private static final PermanentAnyOfPredicate ARTIFACT_OR_CREATURE = new PermanentAnyOfPredicate(List.of(
            new PermanentIsArtifactPredicate(), new PermanentIsCreaturePredicate()));

    public MoxiteRefinery() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{2}",
                List.of(
                        new RemoveOneOrMoreCountersFromControlledPermanentsCost(CounterType.ANY,
                                ARTIFACT_OR_CREATURE),
                        new PutCounterOnTargetPermanentEffect(CounterType.CHARGE, new XValue())
                ),
                "{2}, {T}, Remove X counters from an artifact or creature you control: Put X charge counters on target artifact. "
                        + "Activate only as a sorcery.",
                new ControlledPermanentPredicateTargetFilter(
                        new PermanentIsArtifactPredicate(), "Target must be an artifact you control"),
                null,
                null,
                ActivationTimingRestriction.SORCERY_SPEED
        ).withXValue());

        addActivatedAbility(new ActivatedAbility(
                true,
                "{2}",
                List.of(
                        new RemoveOneOrMoreCountersFromControlledPermanentsCost(CounterType.ANY,
                                ARTIFACT_OR_CREATURE),
                        new PutCounterOnTargetPermanentEffect(CounterType.PLUS_ONE_PLUS_ONE, new XValue())
                ),
                "{2}, {T}, Remove X counters from an artifact or creature you control: Put X +1/+1 counters on target creature. "
                        + "Activate only as a sorcery.",
                new ControlledPermanentPredicateTargetFilter(
                        new PermanentIsCreaturePredicate(), "Target must be a creature you control"),
                null,
                null,
                ActivationTimingRestriction.SORCERY_SPEED
        ).withXValue());
    }
}
