package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.condition.Condition;
import com.github.laxika.magicalvibes.model.condition.TargetPermanentMatches;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourcePermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "WTH", collectorNumber = "156")
@CardRegistration(set = "DDF", collectorNumber = "46")
public class SerratedBiskelion extends Card {

    public SerratedBiskelion() {
        // Targeting itself puts both counters on it as one event, so "one or more counters" triggers fire once.
        Condition selfTarget = new TargetPermanentMatches(new PermanentIsSourcePermanentPredicate());
        Condition otherCreatureTarget = new TargetPermanentMatches(
                new PermanentNotPredicate(new PermanentIsSourcePermanentPredicate()));
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(
                        ConditionalEffect.unless(otherCreatureTarget,
                                new PutCountersOnSourceEffect(-1, -1, 1)),
                        ConditionalEffect.unless(otherCreatureTarget,
                                new PutCounterOnTargetPermanentEffect(CounterType.MINUS_ONE_MINUS_ONE, 1)),
                        ConditionalEffect.unless(selfTarget,
                                new PutCounterOnTargetPermanentEffect(CounterType.MINUS_ONE_MINUS_ONE, 2))
                ),
                "{T}: Put a -1/-1 counter on this creature and a -1/-1 counter on target creature.",
                TargetFilters.creature()
        ));
    }
}
