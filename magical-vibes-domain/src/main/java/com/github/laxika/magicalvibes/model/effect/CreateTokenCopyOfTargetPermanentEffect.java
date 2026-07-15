package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Creates a token that's a copy of the permanent referenced by the stack entry's targetId.
 * Used for "create a token that's a copy of that artifact/creature" triggered abilities
 * where the permanent to copy is determined at trigger time (e.g. Mirrorworks).
 * The token copies all copiable characteristics per CR 707.2.
 *
 * <p>Optional overrides support "except it's X/Y", "except it's also a [type]",
 * additional subtypes, and counters placed after the token enters.
 */
public record CreateTokenCopyOfTargetPermanentEffect(
        List<CardSubtype> additionalSubtypes,
        Set<CardType> additionalTypes,
        Integer powerOverride,
        Integer toughnessOverride,
        Map<CounterType, Integer> initialCounters,
        boolean grantHaste,
        boolean exileAtEndStep
) implements CardEffect {

    public CreateTokenCopyOfTargetPermanentEffect() {
        this(List.of(), Set.of(), null, null, Map.of(), false, false);
    }

    /** "except it has haste and 'At the beginning of the end step, exile this token.'" (Heat Shimmer). */
    public CreateTokenCopyOfTargetPermanentEffect(boolean grantHaste, boolean exileAtEndStep) {
        this(List.of(), Set.of(), null, null, Map.of(), grantHaste, exileAtEndStep);
    }

    public CreateTokenCopyOfTargetPermanentEffect(
            List<CardSubtype> additionalSubtypes,
            Set<CardType> additionalTypes,
            Integer powerOverride,
            Integer toughnessOverride,
            Map<CounterType, Integer> initialCounters) {
        this(additionalSubtypes, additionalTypes, powerOverride, toughnessOverride, initialCounters, false, false);
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetCategory.PERMANENT);
    }
}
