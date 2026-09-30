package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;

import java.util.Objects;

/** Conjures a random implemented creature card with the evaluated mana value onto the battlefield. */
public record ConjureRandomCreatureWithManaValueEffect(DynamicAmount manaValue, boolean temporary)
        implements CardEffect {

    /** Uses the temporary, hasty form that is exiled at the next end step. */
    public ConjureRandomCreatureWithManaValueEffect(DynamicAmount manaValue) {
        this(manaValue, true);
    }

    public ConjureRandomCreatureWithManaValueEffect {
        Objects.requireNonNull(manaValue, "Mana value cannot be null");
    }
}
