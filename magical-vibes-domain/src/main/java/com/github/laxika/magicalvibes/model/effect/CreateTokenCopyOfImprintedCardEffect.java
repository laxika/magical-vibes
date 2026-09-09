package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;

import java.util.List;
import java.util.Set;

/**
 * Creates a token that's a copy of the card imprinted on the source permanent.
 *
 * @param grantHaste     if true, the token gains haste (e.g. Mimic Vat)
 * @param exileAtEndStep if true, the token is exiled at the beginning of the next end step (e.g. Mimic Vat)
 */
public record CreateTokenCopyOfImprintedCardEffect(
        boolean grantHaste,
        boolean exileAtEndStep,
        List<CardSubtype> additionalSubtypes,
        Set<CardType> additionalTypes,
        Integer powerOverride,
        Integer toughnessOverride,
        boolean grantHasteUntilEndOfTurn,
        List<CardEffect> additionalEffects,
        boolean requiresImprintedXCost
) implements ImprintedCardXCostEffect {

    public CreateTokenCopyOfImprintedCardEffect {
        additionalSubtypes = additionalSubtypes == null ? List.of() : List.copyOf(additionalSubtypes);
        additionalTypes = additionalTypes == null ? Set.of() : Set.copyOf(additionalTypes);
        additionalEffects = additionalEffects == null ? List.of() : List.copyOf(additionalEffects);
    }

    /**
     * Mimic Vat's ability has a fixed cost, while Prototype Portal ties X to the imprinted card's
     * mana value.
     */
    public CreateTokenCopyOfImprintedCardEffect(boolean grantHaste, boolean exileAtEndStep) {
        this(grantHaste, exileAtEndStep, List.of(), Set.of(), null, null, false, List.of(),
                !exileAtEndStep);
    }

    /**
     * Backward-compatible constructor for Mimic Vat: grants haste and exiles at end step.
     */
    public CreateTokenCopyOfImprintedCardEffect() {
        this(true, true, List.of(), Set.of(), null, null, false, List.of(), false);
    }
}
