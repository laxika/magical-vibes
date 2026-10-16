package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import java.util.List;
import java.util.Set;

/**
 * Makes the source permanent a permanent copy of a target creature.
 *
 * <p>{@code additionalKeywords} are keywords the copy has as a copiable value ("except it has vigilance"),
 * so they are carried by the copy's characteristics and inherited by later copies of it.
 */
public record BecomeCopyOfTargetCreaturePermanentlyEffect(
        String nameOverride,
        EffectSlot retainedEffectSlot,
        PermanentPredicate targetPredicate,
        List<EffectSlot> additionalRetainedEffectSlots,
        TargetPredicate targetType,
        Set<Keyword> additionalKeywords
) implements CardEffect {

    public BecomeCopyOfTargetCreaturePermanentlyEffect {
        additionalRetainedEffectSlots = additionalRetainedEffectSlots == null
                ? List.of() : List.copyOf(additionalRetainedEffectSlots);
        targetType = targetType == null ? TargetPredicates.creature() : targetType;
        additionalKeywords = additionalKeywords == null ? Set.of() : Set.copyOf(additionalKeywords);
    }

    public BecomeCopyOfTargetCreaturePermanentlyEffect(
            String nameOverride,
            EffectSlot retainedEffectSlot,
            PermanentPredicate targetPredicate,
            List<EffectSlot> additionalRetainedEffectSlots,
            TargetPredicate targetType) {
        this(nameOverride, retainedEffectSlot, targetPredicate, additionalRetainedEffectSlots, targetType, Set.of());
    }

    /** Backward-compatible constructor for creature-copy effects. */
    public BecomeCopyOfTargetCreaturePermanentlyEffect(
            String nameOverride,
            EffectSlot retainedEffectSlot,
            PermanentPredicate targetPredicate,
            List<EffectSlot> additionalRetainedEffectSlots) {
        this(nameOverride, retainedEffectSlot, targetPredicate, additionalRetainedEffectSlots,
                TargetPredicates.creature(), Set.of());
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
                null, retainedEffectSlot, targetPredicate, List.of(), TargetPredicates.permanent(), Set.of());
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(targetType, targetPredicate);
    }
}
