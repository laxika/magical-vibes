package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * "You may sacrifice another creature. When you do, this creature deals damage equal to that
 * creature's power to any target." (Heart-Piercer Manticore's enter trigger.)
 *
 * <p>Placed inside a {@link MayEffect} on {@code ON_ENTER_BATTLEFIELD} or another triggered
 * ability. The original trigger is not targeted. If the controller accepts and sacrifices another
 * creature, the reflexive triggered ability is created and its target is chosen at that point.
 * The sacrificed creature's effective power is captured before it leaves the battlefield and the
 * source permanent deals that much damage to the chosen target. A non-null {@code targetPredicate}
 * restricts that deferred target to matching permanents; {@code null} allows any target.
 */
public record SacrificeAnotherCreatureDealPowerDamageToAnyTargetEffect(PermanentPredicate targetPredicate)
        implements CardEffect {

    /** Creates the any-target variant used by Heart-Piercer Manticore and Grab the Reins. */
    public SacrificeAnotherCreatureDealPowerDamageToAnyTargetEffect() {
        this(null);
    }
}
