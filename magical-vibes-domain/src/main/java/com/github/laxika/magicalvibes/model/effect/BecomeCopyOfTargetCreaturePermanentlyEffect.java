package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import java.util.List;

/** Makes the source permanent a permanent copy of a target creature. */
public record BecomeCopyOfTargetCreaturePermanentlyEffect(
        String nameOverride,
        EffectSlot retainedEffectSlot,
        PermanentPredicate targetPredicate,
        List<EffectSlot> additionalRetainedEffectSlots,
        TargetPredicate targetType
) implements CardEffect {

    public BecomeCopyOfTargetCreaturePermanentlyEffect {
        additionalRetainedEffectSlots = additionalRetainedEffectSlots == null
                ? List.of() : List.copyOf(additionalRetainedEffectSlots);
        targetType = targetType == null ? TargetPredicates.creature() : targetType;
    }

    /** Backward-compatible constructor for creature-copy effects. */
    public BecomeCopyOfTargetCreaturePermanentlyEffect(
            String nameOverride,
            EffectSlot retainedEffectSlot,
            PermanentPredicate targetPredicate,
            List<EffectSlot> additionalRetainedEffectSlots) {
        this(nameOverride, retainedEffectSlot, targetPredicate, additionalRetainedEffectSlots,
                TargetPredicates.creature());
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

    /** Permanent-copy variant used when the copied source is not restricted to creatures. */
    public static BecomeCopyOfTargetCreaturePermanentlyEffect forTargetPermanent(
            EffectSlot retainedEffectSlot, PermanentPredicate targetPredicate) {
        return new BecomeCopyOfTargetCreaturePermanentlyEffect(
                null, retainedEffectSlot, targetPredicate, List.of(), TargetPredicates.permanent());
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(targetType, targetPredicate);
    }
}
