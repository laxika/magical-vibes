package com.github.laxika.magicalvibes.model.effect;

/**
 * "Target opponent chooses a creature they control. Destroy that creature." (Imperial Edict).
 *
 * <p>An edict that <em>destroys</em> the chosen creature rather than sacrificing it, so
 * regeneration and indestructible apply (unlike the {@link SacrificePermanentsEffect} family).
 * The targeted player chooses which of their creatures to lose; with 0 creatures nothing happens,
 * with exactly 1 it is destroyed automatically.
 */
public record TargetPlayerChoosesCreatureDestroyEffect() implements CardEffect {
    @Override public boolean canTargetPlayer() { return true; }
}
