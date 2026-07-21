package com.github.laxika.magicalvibes.model.effect;

/**
 * "Target opponent exiles a creature they control." (Doomfall mode 1).
 *
 * <p>The exile analog of {@link TargetPlayerChoosesCreatureDestroyEffect}: the targeted player
 * chooses which of their creatures to lose, but it is <em>exiled</em> rather than destroyed, so
 * regeneration and indestructible do not apply and no "dies" triggers fire. With 0 creatures
 * nothing happens; with exactly 1 it is exiled automatically; with 2+ the target player picks.
 */
public record TargetPlayerChoosesCreatureExileEffect() implements CardEffect {
    @Override public TargetSpec targetSpec() { return TargetSpec.benign(TargetCategory.PLAYER); }
}
