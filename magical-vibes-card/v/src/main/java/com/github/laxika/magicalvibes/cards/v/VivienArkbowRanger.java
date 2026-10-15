package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.DistributeCountersAmongTargetsEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.SearchOutsideGameForCreatureToHandEffect;
import com.github.laxika.magicalvibes.model.effect.TargetDealsPowerDamageToTargetEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsPlaneswalkerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "M20", collectorNumber = "199")
public class VivienArkbowRanger extends Card {

    public VivienArkbowRanger() {
        addActivatedAbility(new ActivatedAbility(
                false, null,
                List.of(
                        DistributeCountersAmongTargetsEffect.evenlyAmongTargets(
                                CounterType.PLUS_ONE_PLUS_ONE, 2),
                        new GrantKeywordEffect(Keyword.TRAMPLE, GrantScope.TARGET)
                ),
                "+1: Distribute two +1/+1 counters among up to two target creatures. They gain trample until end of turn.",
                null, +1, null, null,
                List.of(TargetFilters.creature(), TargetFilters.creature()), 0, 2
        ));

        addActivatedAbility(new ActivatedAbility(
                false, null,
                List.of(new TargetDealsPowerDamageToTargetEffect()),
                "\u22123: Target creature you control deals damage equal to its power to target creature or planeswalker.",
                null, -3, null, null,
                List.of(
                        TargetFilters.creatureYouControl(),
                        new PermanentPredicateTargetFilter(
                                new PermanentAnyOfPredicate(List.of(
                                        new PermanentIsCreaturePredicate(),
                                        new PermanentIsPlaneswalkerPredicate()
                                )),
                                "Target must be a creature or planeswalker"
                        )
                ), 2, 2
        ));

        addActivatedAbility(new ActivatedAbility(
                -5,
                List.of(new SearchOutsideGameForCreatureToHandEffect()),
                "\u22125: You may reveal a creature card you own from outside the game and put it into your hand."
        ));
    }
}
