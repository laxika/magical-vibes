package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;

import java.util.List;
import java.util.Set;

/**
 * Creates a token that's a copy of the card imprinted on the source permanent.
 *
 * @param grantHaste     if true, the token copy has haste as a copiable value
 * @param exileAtEndStep if true, the token is exiled at the beginning of the next end step (e.g. Mimic Vat)
 * @param exiledLink     the card copied is the one last exiled with the source under this ability link, not its
 *                       imprinted card: the ability borrowed together with the exile that records that link, so the
 *                       pair stays linked on the borrower (CR 607.5); {@code null} for the source's imprint
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
        boolean requiresImprintedXCost,
        String exiledLink
) implements ImprintedCardXCostEffect {

    public CreateTokenCopyOfImprintedCardEffect {
        additionalSubtypes = additionalSubtypes == null ? List.of() : List.copyOf(additionalSubtypes);
        additionalTypes = additionalTypes == null ? Set.of() : Set.copyOf(additionalTypes);
        additionalEffects = additionalEffects == null ? List.of() : List.copyOf(additionalEffects);
    }

    public CreateTokenCopyOfImprintedCardEffect(boolean grantHaste, boolean exileAtEndStep,
                                                List<CardSubtype> additionalSubtypes, Set<CardType> additionalTypes,
                                                Integer powerOverride, Integer toughnessOverride,
                                                boolean grantHasteUntilEndOfTurn, List<CardEffect> additionalEffects,
                                                boolean requiresImprintedXCost) {
        this(grantHaste, exileAtEndStep, additionalSubtypes, additionalTypes, powerOverride, toughnessOverride,
                grantHasteUntilEndOfTurn, additionalEffects, requiresImprintedXCost, null);
    }

    /** This copy of the card last exiled with the source under {@code link} (a borrowed linked pair, CR 607.5). */
    public CreateTokenCopyOfImprintedCardEffect ofCardExiledBy(String link) {
        return new CreateTokenCopyOfImprintedCardEffect(grantHaste, exileAtEndStep, additionalSubtypes, additionalTypes,
                powerOverride, toughnessOverride, grantHasteUntilEndOfTurn, additionalEffects, requiresImprintedXCost,
                link);
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
        this(false, true, List.of(), Set.of(), null, null, true, List.of(), false);
    }
}
