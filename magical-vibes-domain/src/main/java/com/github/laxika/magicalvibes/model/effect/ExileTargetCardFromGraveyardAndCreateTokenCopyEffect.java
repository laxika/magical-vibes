package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;

import java.util.List;

/**
 * Exiles the targeted card from a graveyard, then creates a token that is a copy of that card.
 * Optional additional subtypes are granted to the token "in addition to its other types".
 * Optionally grants haste and/or schedules the token for exile at the beginning of the next end step.
 *
 * @param filter              predicate restricting valid graveyard targets; {@code null} means any card
 * @param ownGraveyardOnly    when {@code true}, only the controller's graveyard can be targeted
 * @param additionalSubtypes  subtypes added to the token copy (e.g. Spirit for Séance)
 * @param grantHaste          when {@code true}, the token gains haste
 * @param exileAtEndStep      when {@code true}, the token is exiled at the beginning of the next end step
 */
public record ExileTargetCardFromGraveyardAndCreateTokenCopyEffect(
        CardPredicate filter,
        boolean ownGraveyardOnly,
        List<CardSubtype> additionalSubtypes,
        boolean grantHaste,
        boolean exileAtEndStep
) implements CardEffect {

    @Override
    public boolean canTargetGraveyard() {
        return true;
    }

    @Override
    public boolean canTargetAnyGraveyard() {
        return !ownGraveyardOnly;
    }
}
