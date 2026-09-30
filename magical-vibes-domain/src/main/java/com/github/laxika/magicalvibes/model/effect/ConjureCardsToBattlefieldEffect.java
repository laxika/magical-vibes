package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;

import java.util.Objects;

/** Conjures a dynamic number of named cards onto the resolving controller's battlefield. */
public record ConjureCardsToBattlefieldEffect(String cardName, DynamicAmount count)
        implements CardEffect {

    public ConjureCardsToBattlefieldEffect {
        Objects.requireNonNull(cardName, "Card name cannot be null");
        Objects.requireNonNull(count, "Count cannot be null");
    }
}
