package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DoesntUntapEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTappedPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

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
        addEffect(EffectSlot.SAGA_CHAPTER_I, new TapPermanentsEffect(TapUntapScope.TARGET));
        addEffect(EffectSlot.SAGA_CHAPTER_I, DoesntUntapEffect.targetWhileSourceOnBattlefield());
        setSagaChapterTargetFilter(EffectSlot.SAGA_CHAPTER_I, Set.of(
                new PermanentPredicateTargetFilter(
                        new PermanentNotPredicate(new PermanentControlledBySourceControllerPredicate()),
                        "Must target a creature an opponent controls"
                )
        ));

        // Chapter II: Same as chapter I
        addEffect(EffectSlot.SAGA_CHAPTER_II, new TapPermanentsEffect(TapUntapScope.TARGET));
        addEffect(EffectSlot.SAGA_CHAPTER_II, DoesntUntapEffect.targetWhileSourceOnBattlefield());
        setSagaChapterTargetFilter(EffectSlot.SAGA_CHAPTER_II, Set.of(
                new PermanentPredicateTargetFilter(
                        new PermanentNotPredicate(new PermanentControlledBySourceControllerPredicate()),
                        "Must target a creature an opponent controls"
                )
        ));

        // Chapter III: Return all tapped creatures to their owners' hands.
        addEffect(EffectSlot.SAGA_CHAPTER_III, ReturnToHandEffect.allPermanentsMatching(
                new PermanentAllOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentIsTappedPredicate()
                ))));
    }
}
