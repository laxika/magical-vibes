package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.EffectSlot;

/**
 * Causes the source permanent to become a copy of the target creature,
 * except it retains the triggered ability that granted this copy effect.
 * Used by Cryptoplasm and similar shapeshifters.
 */
public record BecomeCopyOfTargetCreatureEffect(EffectSlot retainedEffectSlot, boolean copyColor) implements CardEffect {

    public BecomeCopyOfTargetCreatureEffect() {
        this(EffectSlot.UPKEEP_TRIGGERED, true);
    }

    public BecomeCopyOfTargetCreatureEffect(EffectSlot retainedEffectSlot) {
        this(retainedEffectSlot, true);
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.creature());
    }
}
