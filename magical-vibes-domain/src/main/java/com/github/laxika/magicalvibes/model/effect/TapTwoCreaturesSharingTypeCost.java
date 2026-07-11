package com.github.laxika.magicalvibes.model.effect;

/**
 * Cost effect that taps two untapped creatures the controller controls that share a creature type
 * with each other (Changeling-aware). Used by Weight of Conscience.
 *
 * <p>Unlike {@link TapMultiplePermanentsCost}, the two chosen creatures must share a creature type
 * <em>with each other</em>, a mutual constraint the plain multi-tap filter cannot express. Payment
 * is driven one choice at a time: the first pick must be part of some sharing pair, and each
 * subsequent pick must share a creature type with the first-chosen creature.</p>
 */
public record TapTwoCreaturesSharingTypeCost() implements CostEffect {
}
