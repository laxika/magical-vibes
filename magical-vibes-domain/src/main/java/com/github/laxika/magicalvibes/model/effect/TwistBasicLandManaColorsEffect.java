package com.github.laxika.magicalvibes.model.effect;

/**
 * Capability marker for Reality Twist-style global mana replacement: while any permanent with this
 * effect is on the battlefield, lands with basic land types produce remapped colors when tapped
 * for mana (Plains→{R}, Swamps→{G}, Mountains→{W}, Forests→{B}; Islands unchanged).
 *
 * <p>Detected via {@code instanceof TwistBasicLandManaColorsEffect} in
 * {@code GameQueryService} (capability interface — not a concrete-effect dispatch).
 */
public interface TwistBasicLandManaColorsEffect extends CardEffect {
}
