package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.ManaColor;

/**
 * Static effect preventing mana of the given color from draining for this permanent's controller.
 * A {@code null} color prevents all players' mana from draining, as with Upwelling.
 */
public record PreventManaDrainEffect(ManaColor color) implements CardEffect {

    /** Prevents all players' mana from draining. */
    public PreventManaDrainEffect() {
        this(null);
    }
}
