package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DoesntUntapEffect;
import com.github.laxika.magicalvibes.model.effect.MayNotUntapDuringUntapStepEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnUntapLockedPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveCountersWhenUntapLockEndsEffect;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTappedPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "HML", collectorNumber = "28")
@CardRegistration(set = "TSB", collectorNumber = "22")
public class GiantOyster extends Card {

    public GiantOyster() {
        // "You may choose not to untap this creature during your untap step."
        addEffect(EffectSlot.STATIC, new MayNotUntapDuringUntapStepEffect());

        // "{T}: For as long as this creature remains tapped, target tapped creature doesn't untap
        // during its controller's untap step ..." — same untap lock as Rust Tick. The companion
        // TapPermanentsEffect carries the targeting declaration; it is a no-op on the target itself,
        // which the filter already requires to be tapped.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(
                        new TapPermanentsEffect(TapUntapScope.TARGET),
                        DoesntUntapEffect.targetWhileSourceTapped()
                ),
                "{T}: For as long as Giant Oyster remains tapped, target tapped creature doesn't untap during its controller's untap step, and at the beginning of each of your draw steps, put a -1/-1 counter on that creature.",
                new PermanentPredicateTargetFilter(
                        new PermanentAllOfPredicate(List.of(
                                new PermanentIsCreaturePredicate(),
                                new PermanentIsTappedPredicate())),
                        "Target must be a tapped creature")
        ));

        // "... and at the beginning of each of your draw steps, put a -1/-1 counter on that creature."
        addEffect(EffectSlot.DRAW_TRIGGERED,
                new PutCountersOnUntapLockedPermanentsEffect(CounterType.MINUS_ONE_MINUS_ONE, 1));

        // "When this creature leaves the battlefield or becomes untapped, remove all -1/-1 counters
        // from the creature."
        addEffect(EffectSlot.STATIC,
                new RemoveCountersWhenUntapLockEndsEffect(CounterType.MINUS_ONE_MINUS_ONE));
    }
}
