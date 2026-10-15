package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.LifeGainedThisTurn;
import com.github.laxika.magicalvibes.model.amount.LifeLostThisTurn;
import com.github.laxika.magicalvibes.model.amount.Max;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourcePermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "C21", collectorNumber = "6")
public class WillowduskEssenceSeer extends Card {

    public WillowduskEssenceSeer() {
        var anotherCreature = new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentNotPredicate(new PermanentIsSourcePermanentPredicate())
        ));
        var lifeAmount = new Max(
                new LifeGainedThisTurn(CountScope.CONTROLLER),
                new LifeLostThisTurn(CountScope.CONTROLLER));

        addActivatedAbility(new ActivatedAbility(
                true,
                "{1}",
                List.of(new PutCounterOnTargetPermanentEffect(CounterType.PLUS_ONE_PLUS_ONE, lifeAmount)),
                "{1}, {T}: Choose another target creature. Put a number of +1/+1 counters on it equal to "
                        + "the amount of life you gained this turn or the amount of life you lost this turn, "
                        + "whichever is greater. Activate only as a sorcery.",
                new PermanentPredicateTargetFilter(anotherCreature, "Target must be another creature"),
                null,
                null,
                ActivationTimingRestriction.SORCERY_SPEED
        ));
    }
}
