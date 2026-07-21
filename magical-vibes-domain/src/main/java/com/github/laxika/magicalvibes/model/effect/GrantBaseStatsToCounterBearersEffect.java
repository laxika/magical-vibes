package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;

import java.util.Set;

/**
 * Establishes a game-wide continuous rule: every creature with a {@code counterType} counter has
 * base power and toughness {@code power}/{@code toughness} and has the given {@code keywords}, for
 * as long as it has such a counter (CR 613 — layer 7b for the base P/T set, layer 6 for the
 * keywords). Modeled after Aven Mimeomancer's "that creature has base power and toughness 3/1 and
 * has flying for as long as it has a feather counter on it".
 *
 * <p>Resolving this effect adds source-independent
 * {@link com.github.laxika.magicalvibes.model.layer.FloatingContinuousEffect} entries scoped by
 * {@link com.github.laxika.magicalvibes.model.filter.PermanentHasCountersPredicate}. Because the
 * floating effects have no source permanent and a {@code PERMANENT} duration, the rule survives the
 * creating card leaving the battlefield (per the official ruling) yet stops applying to any
 * creature the moment its counter is removed — the scope predicate is re-evaluated every layered
 * pass. The handler is idempotent: establishing the same rule again (e.g. a second copy of the
 * source) adds nothing.
 */
public record GrantBaseStatsToCounterBearersEffect(CounterType counterType, int power, int toughness,
                                                   Set<Keyword> keywords) implements CardEffect {
}
