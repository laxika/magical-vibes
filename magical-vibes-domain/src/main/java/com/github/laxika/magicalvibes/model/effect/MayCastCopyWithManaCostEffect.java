package com.github.laxika.magicalvibes.model.effect;

/** Internal may-ability marker for casting a copy with a specified mana cost. */
public record MayCastCopyWithManaCostEffect(String manaCost) implements CardEffect {

    public MayCastCopyWithManaCostEffect {
        if (manaCost == null || manaCost.isBlank()) {
            throw new IllegalArgumentException("manaCost must not be blank");
        }
    }
}
