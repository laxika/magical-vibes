#!/usr/bin/env python3
"""Step 15, run 17z11e3: Giant Oyster's draw-step counters and counter removal are delayed triggered abilities its
activation creates (CR 603.7e). Idempotent; optional repository-root argument."""
import pathlib
import sys

root = pathlib.Path(sys.argv[1] if len(sys.argv) > 1 else '.')
path = root / 'magical-vibes-card/g/src/main/java/com/github/laxika/magicalvibes/cards/g/GiantOyster.java'

CONTENT = '''package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.CounterRemovalSubject;
import com.github.laxika.magicalvibes.model.effect.DoesntUntapEffect;
import com.github.laxika.magicalvibes.model.effect.MayNotUntapDuringUntapStepEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedDrawStepTriggerWhileSourceTappedEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedSourceUntapsOrLeavesTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveAllCountersEffect;
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
        addStaticEffect(new MayNotUntapDuringUntapStepEffect());

        // The companion TapPermanentsEffect carries the targeting declaration; it is a no-op on the target itself,
        // which the filter already requires to be tapped. The activation creates the two delayed triggered
        // abilities: the draw-step counters for as long as this stays tapped, and the counter removal.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(
                        new TapPermanentsEffect(TapUntapScope.TARGET),
                        DoesntUntapEffect.targetWhileSourceTapped(),
                        new RegisterDelayedDrawStepTriggerWhileSourceTappedEffect(
                                new PutCounterOnTargetPermanentEffect(CounterType.MINUS_ONE_MINUS_ONE, 1)),
                        new RegisterDelayedSourceUntapsOrLeavesTriggerEffect(
                                new RemoveAllCountersEffect(CounterType.MINUS_ONE_MINUS_ONE,
                                        CounterRemovalSubject.TARGET))
                ),
                "{T}: For as long as Giant Oyster remains tapped, target tapped creature doesn't untap during its controller's untap step, and at the beginning of each of your draw steps, put a -1/-1 counter on that creature. When Giant Oyster leaves the battlefield or becomes untapped, remove all -1/-1 counters from the creature.",
                new PermanentPredicateTargetFilter(
                        new PermanentAllOfPredicate(List.of(
                                new PermanentIsCreaturePredicate(),
                                new PermanentIsTappedPredicate())),
                        "Target must be a tapped creature")
        ));
    }
}
'''

if path.read_text() != CONTENT:
    path.write_text(CONTENT)
    print('converted', path)
else:
    print('unchanged', path)
