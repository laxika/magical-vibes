package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DoesntUntapEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTappedPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

/**
 * Time of Ice — {3}{U} Enchantment — Saga
 *
 * (As this Saga enters and after your draw step, add a lore counter. Sacrifice after III.)
 * I, II — Tap target creature an opponent controls. It doesn't untap during its controller's
 *          untap step for as long as you control Time of Ice.
 * III — Return all tapped creatures to their owners' hands.
 */
@CardRegistration(set = "DOM", collectorNumber = "70")
public class TimeOfIce extends Card {

    public TimeOfIce() {
        // Chapter I: Tap target creature an opponent controls.
        // It doesn't untap during its controller's untap step for as long as you control this Saga.
        addTrigger(Triggers.chapter(1),
                new TapPermanentsEffect(TapUntapScope.TARGET),
                DoesntUntapEffect.targetWhileSourceControlled());
        setSagaChapterTargetFilter(1, Set.of(
                new PermanentPredicateTargetFilter(
                        new PermanentAllOfPredicate(List.of(
                                new PermanentIsCreaturePredicate(),
                                new PermanentNotPredicate(new PermanentControlledBySourceControllerPredicate()))),
                        "Must target a creature an opponent controls"
                )
        ));
        setSagaChapterTargetRequired(1);

        // Chapter II: Same as chapter I
        addTrigger(Triggers.chapter(2),
                new TapPermanentsEffect(TapUntapScope.TARGET),
                DoesntUntapEffect.targetWhileSourceControlled());
        setSagaChapterTargetFilter(2, Set.of(
                new PermanentPredicateTargetFilter(
                        new PermanentAllOfPredicate(List.of(
                                new PermanentIsCreaturePredicate(),
                                new PermanentNotPredicate(new PermanentControlledBySourceControllerPredicate()))),
                        "Must target a creature an opponent controls"
                )
        ));
        setSagaChapterTargetRequired(2);

        // Chapter III: Return all tapped creatures to their owners' hands.
        addTrigger(Triggers.chapter(3), ReturnToHandEffect.allPermanentsMatching(
                new PermanentAllOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentIsTappedPredicate()
                ))));
    }
}
