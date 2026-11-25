package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.AddAnotherCounterOfChosenTypeToTargetEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.SagaChapterTargetGroup;
import com.github.laxika.magicalvibes.model.effect.PutCountersEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTappedPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "WHO", collectorNumber = "15")
@CardRegistration(set = "WHO", collectorNumber = "620")
public class TheCavesOfAndrozani extends Card {

    /** "For each non-Saga permanent, choose a counter on it. You may put an additional counter of that kind on it." */
    private static final AddAnotherCounterOfChosenTypeToTargetEffect EACH_NON_SAGA_PERMANENT =
            new AddAnotherCounterOfChosenTypeToTargetEffect(Subjects.controlledBy(Subjects.eachPlayer(),
                    new PermanentNotPredicate(new PermanentHasSubtypePredicate(CardSubtype.SAGA))));

    public TheCavesOfAndrozani() {
        addTrigger(Triggers.chapter(1),
                new PutCountersEffect(CounterType.STUN, 2));
        setSagaChapterTargetGroups(1, List.of(new SagaChapterTargetGroup(
                new PermanentPredicateTargetFilter(
                        new PermanentAllOfPredicate(List.of(
                                new PermanentIsCreaturePredicate(),
                                new PermanentIsTappedPredicate())),
                        "Target must be a tapped creature"),
                0,
                2)));

        addTrigger(Triggers.chapter(2),
                EACH_NON_SAGA_PERMANENT);
        addTrigger(Triggers.chapter(3),
                EACH_NON_SAGA_PERMANENT);

        addTrigger(Triggers.chapter(4),
                new SearchLibraryEffect(new CardSubtypePredicate(CardSubtype.DOCTOR),
                        LibrarySearchDestination.HAND));
    }
}
