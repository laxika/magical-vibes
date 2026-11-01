package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.EffectSlot;

/**
 * Causes the source permanent to become a copy of another target creature,
 * except it retains the triggered ability that granted this copy effect.
 * Used by Cryptoplasm and similar shapeshifters.
 *
 * @param retainedEffectSlot the slot of the triggered ability the copy keeps, or {@code null} for the triggered
 *                           ability with a real trigger whose effects hold this effect
 */
public record BecomeCopyOfTargetCreatureEffect(
        EffectSlot retainedEffectSlot,
        boolean copyColor,
        boolean canTargetSelf) implements CardEffect {

    public BecomeCopyOfTargetCreatureEffect() {
        this(EffectSlot.UPKEEP_TRIGGERED, true, false);
    }

    public BecomeCopyOfTargetCreatureEffect(EffectSlot retainedEffectSlot) {
        this(retainedEffectSlot, true, false);
    }

    public BecomeCopyOfTargetCreatureEffect(EffectSlot retainedEffectSlot, boolean copyColor) {
        this(retainedEffectSlot, copyColor, false);
    }

    /** A copy effect that keeps the triggered ability holding it, one with a real trigger. */
    public static BecomeCopyOfTargetCreatureEffect retainingItsAbility() {
        return new BecomeCopyOfTargetCreatureEffect(null, true, false);
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.creature());
    }
}
