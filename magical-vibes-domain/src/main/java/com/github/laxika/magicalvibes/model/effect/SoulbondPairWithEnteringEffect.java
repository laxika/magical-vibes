package com.github.laxika.magicalvibes.model.effect;

/**
 * Soulbond "another creature enters" half (CR 702.94a): pair this unpaired creature with the
 * entering unpaired creature. The entering permanent id is baked as the stack entry's
 * {@code targetId} by the enter-trigger collector; typically wrapped in a {@link MayEffect}.
 */
public record SoulbondPairWithEnteringEffect() implements CardEffect {
}
