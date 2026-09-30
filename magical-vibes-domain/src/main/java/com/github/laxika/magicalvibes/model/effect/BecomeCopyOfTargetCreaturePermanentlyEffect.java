package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

import java.util.List;

/** Makes the source permanent a permanent copy of a target creature. */
public record BecomeCopyOfTargetCreaturePermanentlyEffect(
        String nameOverride,
        EffectSlot retainedEffectSlot,
        PermanentPredicate targetPredicate,
        List<EffectSlot> additionalRetainedEffectSlots
) implements CardEffect {

    public BecomeCopyOfTargetCreaturePermanentlyEffect {
        additionalRetainedEffectSlots = additionalRetainedEffectSlots == null
                ? List.of() : List.copyOf(additionalRetainedEffectSlots);
    }

    public BecomeCopyOfTargetCreaturePermanentlyEffect() {
        this(null, null, null, List.of());
    }

    public BecomeCopyOfTargetCreaturePermanentlyEffect(String nameOverride, EffectSlot retainedEffectSlot) {
        this(nameOverride, retainedEffectSlot, null, List.of());
    }

    public BecomeCopyOfTargetCreaturePermanentlyEffect(String nameOverride, EffectSlot retainedEffectSlot,
                                                       PermanentPredicate targetPredicate) {
        this(nameOverride, retainedEffectSlot, targetPredicate, List.of());
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.creature(), targetPredicate);
    }
}
