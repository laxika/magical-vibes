package com.github.laxika.magicalvibes.model.effect;

/**
 * Sacrifices the permanent tracked by the delayed trigger and, if it was a creature, creates a reflexive ability that
 * deals damage equal to its power to any target. The permanent's last-known characteristics are
 * retained so it remains the source of the damage after being sacrificed.
 */
public record SacrificeTargetPermanentThenDealPowerDamageToAnyTargetEffect() implements CardEffect {
}
