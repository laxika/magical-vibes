package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.SagaChapterTargetGroup;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentUntilSourceLeavesEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "FIC", collectorNumber = "27")
@CardRegistration(set = "FIC", collectorNumber = "195")
public class SummonIxion extends Card {

    private static final PermanentPredicate OPPONENT_CREATURE = new PermanentAllOfPredicate(List.of(
            new PermanentIsCreaturePredicate(),
            new PermanentNotPredicate(new PermanentControlledBySourceControllerPredicate())
    ));

    public SummonIxion() {
        addTrigger(Triggers.chapter(1),
                new ExileTargetPermanentUntilSourceLeavesEffect(false, OPPONENT_CREATURE));
        setSagaChapterTargetFilter(1, Set.of(
                new PermanentPredicateTargetFilter(OPPONENT_CREATURE,
                        "Must target a creature an opponent controls")));

        addTrigger(Triggers.chapter(2),
                new PutCounterOnTargetPermanentEffect(CounterType.PLUS_ONE_PLUS_ONE, 1),
                new GainLifeEffect(2));
        setSagaChapterTargetGroups(2, List.of(
                new SagaChapterTargetGroup(TargetFilters.creatureYouControl(), 0, 2)));

        addTrigger(Triggers.chapter(3),
                new PutCounterOnTargetPermanentEffect(CounterType.PLUS_ONE_PLUS_ONE, 1),
                new GainLifeEffect(2));
        setSagaChapterTargetGroups(3, List.of(
                new SagaChapterTargetGroup(TargetFilters.creatureYouControl(), 0, 2)));
    }
}
