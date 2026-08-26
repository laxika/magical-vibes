package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTappedPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "FIN", collectorNumber = "78")
public class SummonShiva extends Card {

    public SummonShiva() {
        var opponentCreature = new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentNotPredicate(new PermanentControlledBySourceControllerPredicate())
        ));

        addEffect(EffectSlot.SAGA_CHAPTER_I, new TapPermanentsEffect(TapUntapScope.TARGET));
        addEffect(EffectSlot.SAGA_CHAPTER_I, new PutCounterOnTargetPermanentEffect(CounterType.STUN));
        setSagaChapterTargetFilter(EffectSlot.SAGA_CHAPTER_I, Set.of(
                new PermanentPredicateTargetFilter(opponentCreature, "Must target a creature an opponent controls")
        ));

        addEffect(EffectSlot.SAGA_CHAPTER_II, new TapPermanentsEffect(TapUntapScope.TARGET));
        addEffect(EffectSlot.SAGA_CHAPTER_II, new PutCounterOnTargetPermanentEffect(CounterType.STUN));
        setSagaChapterTargetFilter(EffectSlot.SAGA_CHAPTER_II, Set.of(
                new PermanentPredicateTargetFilter(opponentCreature, "Must target a creature an opponent controls")
        ));

        addEffect(EffectSlot.SAGA_CHAPTER_III, new DrawCardEffect(new PermanentCount(
                new PermanentAllOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentIsTappedPredicate()
                )), CountScope.OPPONENTS)));
    }
}
