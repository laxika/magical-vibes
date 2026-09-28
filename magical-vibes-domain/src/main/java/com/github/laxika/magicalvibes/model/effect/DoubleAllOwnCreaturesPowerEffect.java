package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Doubles the power of each creature controlled by the effect's controller until end of turn.
 * Each creature's current effective power is added to that creature independently as the effect
 * resolves.
 */
public record DoubleAllOwnCreaturesPowerEffect(PermanentPredicate filter) implements CardEffect, DoublingEffect {

    public DoubleAllOwnCreaturesPowerEffect() {
        this(null);
    }
}
