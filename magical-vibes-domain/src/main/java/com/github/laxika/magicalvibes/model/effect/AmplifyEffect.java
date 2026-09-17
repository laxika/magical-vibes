package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/** As this creature enters, reveal any number of matching cards for counters. */
public record AmplifyEffect(int countersPerCard, CardPredicate filter) implements ReplacementEffect {
}
