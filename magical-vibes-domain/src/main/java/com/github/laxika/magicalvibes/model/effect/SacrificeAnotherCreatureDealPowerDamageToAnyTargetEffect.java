package com.github.laxika.magicalvibes.model.effect;

/**
 * "You may sacrifice another creature. When you do, this creature deals damage equal to that
 * creature's power to any target." (Heart-Piercer Manticore's enter trigger.)
 *
 * <p>Placed inside a {@link MayEffect} on {@code ON_ENTER_BATTLEFIELD}. The original enter trigger
 * is not targeted. If the controller accepts and sacrifices another creature, the reflexive
 * triggered ability is created and its any-target is chosen at that point (CR 603.12).
 * The sacrificed creature's effective power is captured before it leaves the battlefield and the
 * source permanent deals that much damage to the chosen target. Declining, or controlling no other
 * creature, deals no damage.
 */
public record SacrificeAnotherCreatureDealPowerDamageToAnyTargetEffect() implements CardEffect {}
