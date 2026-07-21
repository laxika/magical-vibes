package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantBaseStatsToCounterBearersEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.Set;

@CardRegistration(set = "ARB", collectorNumber = "2")
public class AvenMimeomancer extends Card {

    public AvenMimeomancer() {
        // "that creature has base power and toughness 3/1 and has flying for as long as it has a
        // feather counter on it" — a source-independent continuous rule keyed off feather counters,
        // installed on entry so it persists even after this creature leaves the battlefield.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new GrantBaseStatsToCounterBearersEffect(CounterType.FEATHER, 3, 1, Set.of(Keyword.FLYING)));

        // At the beginning of your upkeep, you may put a feather counter on target creature.
        target(new PermanentPredicateTargetFilter(
                new PermanentIsCreaturePredicate(),
                "Target must be a creature."
        )).addEffect(EffectSlot.UPKEEP_TRIGGERED, new MayEffect(
                new PutCounterOnTargetPermanentEffect(CounterType.FEATHER),
                "Put a feather counter on target creature?"
        ));
    }
}
