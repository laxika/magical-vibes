package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/** Makes the source permanent a permanent copy of a target creature. */
public record BecomeCopyOfTargetCreaturePermanentlyEffect(
        String nameOverride,
        EffectSlot retainedEffectSlot,
        PermanentPredicate targetPredicate
) implements CardEffect {

    public BecomeCopyOfTargetCreaturePermanentlyEffect() {
        this(null, null, null);
    }

    public BecomeCopyOfTargetCreaturePermanentlyEffect(String nameOverride, EffectSlot retainedEffectSlot) {
        this(nameOverride, retainedEffectSlot, null);
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.creature(), targetPredicate);
    }
}
