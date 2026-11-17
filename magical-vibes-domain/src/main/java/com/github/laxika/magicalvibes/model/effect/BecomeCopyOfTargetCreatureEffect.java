package com.github.laxika.magicalvibes.model.effect;

/**
 * Causes the source permanent to become a copy of another target creature,
 * except it retains the triggered ability that granted this copy effect.
 * Used by Cryptoplasm and similar shapeshifters.
 * The copy keeps the triggered ability whose effects hold this effect.
 */
public record BecomeCopyOfTargetCreatureEffect(
        boolean copyColor,
        boolean canTargetSelf) implements CardEffect {

    public BecomeCopyOfTargetCreatureEffect() {
        this(true, false);
    }

    /** A copy effect that keeps the triggered ability holding it, one with a real trigger. */
    public static BecomeCopyOfTargetCreatureEffect retainingItsAbility() {
        return new BecomeCopyOfTargetCreatureEffect(true, false);
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.creature());
    }
}
