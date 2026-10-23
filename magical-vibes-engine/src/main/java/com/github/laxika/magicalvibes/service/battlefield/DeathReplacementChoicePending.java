package com.github.laxika.magicalvibes.service.battlefield;

/**
 * Stops a resolving effect handler marked
 * {@link com.github.laxika.magicalvibes.service.effect.AsksDeathReplacementsFirst} before it has
 * changed anything, because a creature it would kill must first have its CR 616.1 death replacement
 * chosen. {@code EffectResolutionService} catches it and resolves the effect again once answered.
 */
public final class DeathReplacementChoicePending extends RuntimeException {

    public DeathReplacementChoicePending() {
        super("A death replacement must be chosen before the effect resolves", null, false, false);
    }
}
