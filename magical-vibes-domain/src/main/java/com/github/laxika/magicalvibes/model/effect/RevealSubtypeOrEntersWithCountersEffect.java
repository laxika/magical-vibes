package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;

import java.util.Set;

/**
 * As-enters replacement effect: the controller may reveal a matching card from their hand; if
 * they do, the permanent enters with the specified counters.
 */
public record RevealSubtypeOrEntersWithCountersEffect(Set<CardSubtype> subtypes,
                                                      CounterType counterType,
                                                      int counterCount) implements CardEffect {

    public RevealSubtypeOrEntersWithCountersEffect {
        subtypes = Set.copyOf(subtypes);
    }

    public RevealSubtypeOrEntersWithCountersEffect(CardSubtype subtype,
                                                    CounterType counterType,
                                                    int counterCount) {
        this(Set.of(subtype), counterType, counterCount);
    }
}
