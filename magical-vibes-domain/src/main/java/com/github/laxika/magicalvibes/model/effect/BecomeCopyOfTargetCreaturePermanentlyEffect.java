package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.trigger.LegacySlotTrigger;
import com.github.laxika.magicalvibes.model.trigger.Trigger;
import java.util.List;
import java.util.Set;

/**
 * Makes the source permanent a permanent copy of a target creature.
 *
 * <p>{@code additionalKeywords} are keywords the copy has as a copiable value ("except it has vigilance"),
 * so they are carried by the copy's characteristics and inherited by later copies of it.
 *
 * <p>{@code retainedTrigger} names the triggered ability holding this effect that the copy keeps ("except it has
 * this ability"), or is {@code null} when it keeps none. A {@link LegacySlotTrigger} keeps the registration of its
 * slot that holds this effect. {@code additionalRetainedTriggers} names the other triggered abilities the copy keeps
 * ("it has this ability and 'whenever this deals combat damage to a player, …'").
 */
public record BecomeCopyOfTargetCreaturePermanentlyEffect(
        String nameOverride,
        Trigger retainedTrigger,
        PermanentPredicate targetPredicate,
        List<Trigger> additionalRetainedTriggers,
        TargetPredicate targetType,
        Set<Keyword> additionalKeywords
) implements CardEffect {

    public BecomeCopyOfTargetCreaturePermanentlyEffect {
        additionalRetainedTriggers = additionalRetainedTriggers == null
                ? List.of() : List.copyOf(additionalRetainedTriggers);
        targetType = targetType == null ? TargetPredicates.creature() : targetType;
        additionalKeywords = additionalKeywords == null ? Set.of() : Set.copyOf(additionalKeywords);
    }

    public BecomeCopyOfTargetCreaturePermanentlyEffect(
            String nameOverride,
            Trigger retainedTrigger,
            PermanentPredicate targetPredicate,
            List<Trigger> additionalRetainedTriggers,
            TargetPredicate targetType) {
        this(nameOverride, retainedTrigger, targetPredicate, additionalRetainedTriggers, targetType, Set.of());
    }

    /** Backward-compatible constructor for creature-copy effects. */
    public BecomeCopyOfTargetCreaturePermanentlyEffect(
            String nameOverride,
            Trigger retainedTrigger,
            PermanentPredicate targetPredicate,
            List<Trigger> additionalRetainedTriggers) {
        this(nameOverride, retainedTrigger, targetPredicate, additionalRetainedTriggers,
                TargetPredicates.creature(), Set.of());
    }

    public BecomeCopyOfTargetCreaturePermanentlyEffect() {
        this(null, null, null, List.of());
    }

    public BecomeCopyOfTargetCreaturePermanentlyEffect(String nameOverride, Trigger retainedTrigger) {
        this(nameOverride, retainedTrigger, null, List.of());
    }

    public BecomeCopyOfTargetCreaturePermanentlyEffect(String nameOverride, Trigger retainedTrigger,
                                                       PermanentPredicate targetPredicate) {
        this(nameOverride, retainedTrigger, targetPredicate, List.of());
    }

    /** Permanent-copy variant used when the copied source is not restricted to creatures. */
    public static BecomeCopyOfTargetCreaturePermanentlyEffect forTargetPermanent(
            Trigger retainedTrigger, PermanentPredicate targetPredicate) {
        return new BecomeCopyOfTargetCreaturePermanentlyEffect(
                null, retainedTrigger, targetPredicate, List.of(), TargetPredicates.permanent(), Set.of());
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(targetType, targetPredicate);
    }
}
