package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Makes permanents matching {@code filter} that enter under the resolving controller's control
 * enter tapped for the rest of the turn. The no-argument form keeps the existing global effect.
 */
public record PermanentsEnterTappedThisTurnEffect(PermanentPredicate filter) implements CardEffect {

    public PermanentsEnterTappedThisTurnEffect() {
        this(null);
    }
}
