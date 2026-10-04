package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;

import java.util.Set;

/**
 * Gives the referenced creature base power, toughness, and keywords while it retains counters.
 * Place the required counter before this effect in the same targeted ability. Each resolution
 * affects only that creature, survives the source leaving, and expires permanently after its
 * final counter of the specified type is removed.
 */
public record GrantBaseStatsToCounterBearersEffect(CounterType counterType, int power, int toughness,
                                                   Set<Keyword> keywords) implements CardEffect {
}
