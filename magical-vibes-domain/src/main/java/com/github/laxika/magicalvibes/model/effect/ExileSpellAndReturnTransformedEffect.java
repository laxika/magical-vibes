package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CounterType;

/**
 * Exiles a spell cast from a graveyard, then returns its physical card to the battlefield
 * transformed under its owner's control.
 *
 * @param enterWithCounter counter placed on the returned permanent as it enters (Esper Origins'
 *                         finality counter), or {@code null} for none
 */
public record ExileSpellAndReturnTransformedEffect(CounterType enterWithCounter) implements CardEffect {

    public ExileSpellAndReturnTransformedEffect() {
        this(null);
    }
}
