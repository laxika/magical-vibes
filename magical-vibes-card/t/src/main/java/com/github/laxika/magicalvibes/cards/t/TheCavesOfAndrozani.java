package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.SagaChapterTargetGroup;
import com.github.laxika.magicalvibes.model.effect.AddAnotherCounterOfChosenTypeToEachNonSagaPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTappedPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;

import java.util.List;

@CardRegistration(set = "WHO", collectorNumber = "15")
@CardRegistration(set = "WHO", collectorNumber = "620")
public class TheCavesOfAndrozani extends Card {

    public TheCavesOfAndrozani() {
        addEffect(EffectSlot.SAGA_CHAPTER_I,
                new PutCounterOnTargetPermanentEffect(CounterType.STUN, 2));
        setSagaChapterTargetGroups(EffectSlot.SAGA_CHAPTER_I, List.of(new SagaChapterTargetGroup(
                new PermanentPredicateTargetFilter(
                        new PermanentAllOfPredicate(List.of(
                                new PermanentIsCreaturePredicate(),
                                new PermanentIsTappedPredicate())),
                        "Target must be a tapped creature"),
                0,
                2)));

        addEffect(EffectSlot.SAGA_CHAPTER_II,
                new AddAnotherCounterOfChosenTypeToEachNonSagaPermanentEffect());
        addEffect(EffectSlot.SAGA_CHAPTER_III,
                new AddAnotherCounterOfChosenTypeToEachNonSagaPermanentEffect());

        addEffect(EffectSlot.SAGA_CHAPTER_IV,
                new SearchLibraryEffect(new CardSubtypePredicate(CardSubtype.DOCTOR),
                        LibrarySearchDestination.HAND));
    }
}
