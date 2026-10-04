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
@CardRegistration(set = "NCC", collectorNumber = "329")
public class AvenMimeomancer extends Card {

    public AvenMimeomancer() {
        // At the beginning of your upkeep, you may put a feather counter on target creature.
        target(new PermanentPredicateTargetFilter(
                new PermanentIsCreaturePredicate(),
                "Target must be a creature."
        )).addEffect(EffectSlot.UPKEEP_TRIGGERED, new MayEffect(
                com.github.laxika.magicalvibes.model.effect.SequenceEffect.of(
                        new PutCounterOnTargetPermanentEffect(CounterType.FEATHER),
                        new GrantBaseStatsToCounterBearersEffect(CounterType.FEATHER, 3, 1, Set.of(Keyword.FLYING))),
                "Put a feather counter on target creature?"
        ));
    }
}
