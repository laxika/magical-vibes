package com.github.laxika.magicalvibes.model.effect;

/**
 * Marker for the resolution-time "you may cast this for its miracle cost" choice.
 * The miracle cost string is carried on {@code PendingMayAbility.manaCost()}.
 */
public record MayCastForMiracleCostEffect(int xReduction) implements CardEffect {
    public MayCastForMiracleCostEffect() {
        this(0);
    }
}
