package com.github.laxika.magicalvibes.model.effect;

import java.util.List;

/**
 * "You may sacrifice another creature. When you do, this creature deals damage equal to that
 * creature's power to any target." (Heart-Piercer Manticore's enter trigger; Ziatora, the
 * Incinerator adds a Treasure follow-up.)
 *
 * <p>Placed inside a {@link MayEffect} on {@code ON_ENTER_BATTLEFIELD}. The original enter trigger
 * is not targeted. If the controller accepts and sacrifices another creature, the reflexive
 * triggered ability is created and its any-target is chosen at that point (CR 603.12).
 * The sacrificed creature's effective power is captured before it leaves the battlefield and the
 * source permanent deals that much damage to the chosen target. {@code reflexiveFollowUps} are
 * added to that same reflexive ability, after the damage effect, and only exist if a creature was
 * sacrificed. Declining, or controlling no other creature, deals no damage and does not resolve
 * any follow-up effects.
 *
 * @param reflexiveFollowUps effects to add after the damage on the reflexive ability
 */
public record SacrificeAnotherCreatureDealPowerDamageToAnyTargetEffect(
        List<CardEffect> reflexiveFollowUps
) implements CardEffect {

    public SacrificeAnotherCreatureDealPowerDamageToAnyTargetEffect() {
        this(List.of());
    }

    public SacrificeAnotherCreatureDealPowerDamageToAnyTargetEffect(CardEffect... reflexiveFollowUps) {
        this(List.of(reflexiveFollowUps));
    }

    public SacrificeAnotherCreatureDealPowerDamageToAnyTargetEffect {
        reflexiveFollowUps = List.copyOf(reflexiveFollowUps);
    }
}
