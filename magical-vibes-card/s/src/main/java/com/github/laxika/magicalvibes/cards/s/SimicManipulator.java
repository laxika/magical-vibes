package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.ControlDuration;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveOneOrMoreCountersFromSourceCost;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPowerAtMostXPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "GTC", collectorNumber = "50")
public class SimicManipulator extends Card {

    public SimicManipulator() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(
                        new RemoveOneOrMoreCountersFromSourceCost(CounterType.PLUS_ONE_PLUS_ONE),
                        new GainControlOfTargetEffect(ControlDuration.PERMANENT)),
                "{T}, Remove one or more +1/+1 counters from this creature: Gain control of target creature with power less than or equal to the number of +1/+1 counters removed this way.",
                new PermanentPredicateTargetFilter(
                        new PermanentAllOfPredicate(List.of(
                                new PermanentIsCreaturePredicate(),
                                new PermanentPowerAtMostXPredicate())),
                        "Target must be a creature with power less than or equal to the number of counters removed"))
                .withXValue());
    }
}
