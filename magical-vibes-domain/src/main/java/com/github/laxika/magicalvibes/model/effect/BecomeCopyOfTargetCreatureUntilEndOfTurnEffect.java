package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;

import java.util.Set;

/**
 * Causes the source permanent to become a copy of the target creature until end of turn.
 * At the cleanup step, the permanent reverts to its original card.
 * Used by Tilonalli's Skinshifter and similar shapeshifters.
 */
public record BecomeCopyOfTargetCreatureUntilEndOfTurnEffect(
        String nameOverride,
        Set<CardSupertype> additionalSupertypesOverride,
        Integer powerOverride,
        Integer toughnessOverride,
        Set<CardType> additionalTypesOverride,
        Set<CardSubtype> additionalSubtypesOverride,
        Set<Keyword> additionalKeywordsOverride
) implements CardEffect {

    public BecomeCopyOfTargetCreatureUntilEndOfTurnEffect {
        additionalSupertypesOverride = additionalSupertypesOverride == null
                ? Set.of() : Set.copyOf(additionalSupertypesOverride);
        additionalTypesOverride = additionalTypesOverride == null
                ? Set.of() : Set.copyOf(additionalTypesOverride);
        additionalSubtypesOverride = additionalSubtypesOverride == null
                ? Set.of() : Set.copyOf(additionalSubtypesOverride);
        additionalKeywordsOverride = additionalKeywordsOverride == null
                ? Set.of() : Set.copyOf(additionalKeywordsOverride);
    }

    public BecomeCopyOfTargetCreatureUntilEndOfTurnEffect(
            String nameOverride, Set<CardSupertype> additionalSupertypesOverride) {
        this(nameOverride, additionalSupertypesOverride, null, null,
                Set.of(), Set.of(), Set.of());
    }

    public BecomeCopyOfTargetCreatureUntilEndOfTurnEffect(
            Integer powerOverride, Integer toughnessOverride,
            Set<CardType> additionalTypesOverride, Set<CardSubtype> additionalSubtypesOverride,
            Set<Keyword> additionalKeywordsOverride) {
        this(null, Set.of(), powerOverride, toughnessOverride, additionalTypesOverride,
                additionalSubtypesOverride, additionalKeywordsOverride);
    }

    public BecomeCopyOfTargetCreatureUntilEndOfTurnEffect() {
        this(null, Set.of(), null, null, Set.of(), Set.of(), Set.of());
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.permanent());
    }
}
