package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
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
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "FIN", collectorNumber = "78")
@CardRegistration(set = "FIN", collectorNumber = "362")
public class SummonShiva extends Card {

    public SummonShiva() {
        var opponentCreature = new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentNotPredicate(new PermanentControlledBySourceControllerPredicate())
        ));

        addTrigger(Triggers.chapter(1), new TapPermanentsEffect(TapUntapScope.TARGET));
        addTrigger(Triggers.chapter(1), new PutCounterOnTargetPermanentEffect(CounterType.STUN));
        setSagaChapterTargetFilter(1, Set.of(
                new PermanentPredicateTargetFilter(opponentCreature, "Must target a creature an opponent controls")
        ));
        setSagaChapterTargetRequired(1);

        addTrigger(Triggers.chapter(2), new TapPermanentsEffect(TapUntapScope.TARGET));
        addTrigger(Triggers.chapter(2), new PutCounterOnTargetPermanentEffect(CounterType.STUN));
        setSagaChapterTargetFilter(2, Set.of(
                new PermanentPredicateTargetFilter(opponentCreature, "Must target a creature an opponent controls")
        ));
        setSagaChapterTargetRequired(2);

        addTrigger(Triggers.chapter(3), new DrawCardEffect(new PermanentCount(
                new PermanentAllOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentIsTappedPredicate()
                )), CountScope.OPPONENTS)));
    }
}
