package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.EffectSlot;

/**
 * Causes the source permanent to become a copy of another target creature,
 * except it retains the triggered ability that granted this copy effect.
 * Used by Cryptoplasm and similar shapeshifters.
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

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.creature());
    }
}
