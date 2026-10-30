package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;

import java.util.Objects;

/** Conjures a dynamic number of cards of the given printing onto the resolving controller's battlefield. */
public record ConjureCardsToBattlefieldEffect(String setCode, String collectorNumber, DynamicAmount count)
        implements CardEffect {

    public ConjureCardsToBattlefieldEffect {
        Objects.requireNonNull(setCode, "Set code cannot be null");
        Objects.requireNonNull(collectorNumber, "Collector number cannot be null");
        Objects.requireNonNull(count, "Count cannot be null");
    }
}
