package com.github.laxika.magicalvibes.model.effect;

/**
 * Internal may-ability marker for casting an exiled copy while paying its costs: its own mana cost
 * ({@code manaCost} {@code null}, "cast the copy by paying its mana cost") or {@code manaCost} instead
 * ("you may cast the copy by paying {1} rather than paying its mana cost"). Declining leaves the copy out of
 * every zone. A free cast of a copy is {@link MayCastCopyWithoutPayingManaCostEffect}.
 */
public record MayCastCopyWithManaCostEffect(String manaCost) implements CardEffect {

    public MayCastCopyWithManaCostEffect {
        if (manaCost != null && manaCost.isBlank()) {
            throw new IllegalArgumentException("manaCost must not be blank");
        }
    }

    /** The copy cast for its own mana cost. */
    public MayCastCopyWithManaCostEffect() {
        this(null);
    }
}
