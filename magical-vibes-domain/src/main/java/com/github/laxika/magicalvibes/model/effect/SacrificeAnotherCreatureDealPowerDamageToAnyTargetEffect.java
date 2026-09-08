package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import java.util.List;

/**
 * "You may sacrifice another creature. When you do, this creature deals damage equal to that
 * creature's power to any target." (Heart-Piercer Manticore's enter trigger; Ziatora, the
 * Incinerator adds a Treasure follow-up.)
 *
 * <p>Placed inside a {@link MayEffect} on {@code ON_ENTER_BATTLEFIELD} or another triggered
 * ability. The original trigger is not targeted. If the controller accepts and sacrifices another
 * creature, the reflexive triggered ability is created and its target is chosen at that point.
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
, PermanentPredicate targetPredicate) implements CardEffect {
        public SacrificeAnotherCreatureDealPowerDamageToAnyTargetEffect(
        List<CardEffect> reflexiveFollowUps
) {
            this(reflexiveFollowUps, null);
        }


    public SacrificeAnotherCreatureDealPowerDamageToAnyTargetEffect(PermanentPredicate targetPredicate) {
        this(List.of(), targetPredicate);
    }

    public SacrificeAnotherCreatureDealPowerDamageToAnyTargetEffect() {
        this(List.of(), null);
    }

    public SacrificeAnotherCreatureDealPowerDamageToAnyTargetEffect(CardEffect... reflexiveFollowUps) {
        this(List.of(reflexiveFollowUps), null);
    }

    public SacrificeAnotherCreatureDealPowerDamageToAnyTargetEffect {
        reflexiveFollowUps = List.copyOf(reflexiveFollowUps);
    }
}
