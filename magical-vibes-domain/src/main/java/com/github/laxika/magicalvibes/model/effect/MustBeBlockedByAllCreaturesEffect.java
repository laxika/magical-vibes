package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Static Lure-style ability: all creatures matching {@code blockerFilter} that are able to block
 * this permanent must do so. A {@code null} filter means every able creature (classic Lure /
 * Prized Unicorn). Pass e.g. {@code PermanentHasKeywordPredicate(FLYING)} for "all creatures
 * with flying able to block this creature do so" (Talruum Piper).
 */
public record MustBeBlockedByAllCreaturesEffect(PermanentPredicate blockerFilter) implements CardEffect {

    /** Unfiltered lure — all creatures able to block this must do so. */
    public MustBeBlockedByAllCreaturesEffect() {
        this(null);
    }
}
