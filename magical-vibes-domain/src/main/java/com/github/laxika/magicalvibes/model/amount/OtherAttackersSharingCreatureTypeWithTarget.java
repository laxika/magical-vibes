package com.github.laxika.magicalvibes.model.amount;

/**
 * The number of attacking creatures — other than the target permanent itself — that share a
 * creature type with the target (Changeling-aware). Pair with {@code BoostTargetCreatureEffect}
 * on an {@code ON_ALLY_CREATURE_ATTACKS} trigger whose target is set to the triggering attacker
 * (e.g. Shared Animosity: "it gets +1/+0 until end of turn for each other attacking creature that
 * shares a creature type with it").
 */
public record OtherAttackersSharingCreatureTypeWithTarget() implements DynamicAmount {
}
