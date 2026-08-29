package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;

/**
 * Proliferate. Choose any number of permanents and/or players that have a counter,
 * then give each one additional counter of each kind that permanent or player already has.
 */
public record ProliferateEffect(DynamicAmount amount) implements CardEffect {

    /** A normal single proliferate event. */
    public ProliferateEffect() {
        this(null);
    }
}
