package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;

import java.util.List;

/**
 * Exiles the targeted card from a graveyard, then creates a token that is a copy of that card.
 * Optional additional subtypes are granted to the token "in addition to its other types".
 * Optionally grants haste and/or schedules the token for exile at the beginning of the next end step.
 * Optional Eternalize-style overrides ({@code colorOverride}, {@code powerOverride},
 * {@code toughnessOverride}) transform the copy (e.g. The Scarab God: 4/4 black Zombie).
 *
 * @param filter              predicate restricting valid graveyard targets; {@code null} means any card
 * @param ownGraveyardOnly    when {@code true}, only the controller's graveyard can be targeted
 * @param additionalSubtypes  subtypes added to the token copy (e.g. Spirit for Séance, Zombie for Scarab God)
 * @param grantHaste          when {@code true}, the token gains haste
 * @param exileAtEndStep      when {@code true}, the token is exiled at the beginning of the next end step
 * @param colorOverride       if non-null, the token's color is set to exactly this color
 * @param powerOverride       if non-null, the token's base power is set to this
 * @param toughnessOverride   if non-null, the token's base toughness is set to this
 */
public record ExileTargetCardFromGraveyardAndCreateTokenCopyEffect(
        CardPredicate filter,
        boolean ownGraveyardOnly,
        List<CardSubtype> additionalSubtypes,
        boolean grantHaste,
        boolean exileAtEndStep,
        CardColor colorOverride,
        Integer powerOverride,
        Integer toughnessOverride
) implements CardEffect {

    /** Compact form without Eternalize-style P/T/color overrides (Séance). */
    public ExileTargetCardFromGraveyardAndCreateTokenCopyEffect(
            CardPredicate filter,
            boolean ownGraveyardOnly,
            List<CardSubtype> additionalSubtypes,
            boolean grantHaste,
            boolean exileAtEndStep) {
        this(filter, ownGraveyardOnly, additionalSubtypes, grantHaste, exileAtEndStep, null, null, null);
    }

    @Override
    public TargetSpec targetSpec() {
        // ownGraveyardOnly narrows the search but was NEVER expressed through
        // targetsControllersGraveyardOnly (which stayed false) — it is the kept validator's
        // controller-compare. So ownGraveyardOnly=true reproduces (graveyard=T, any=F) = GRAVEYARD_CARD,
        // not CONTROLLERS_GRAVEYARD_CARD.
        return ownGraveyardOnly
                ? TargetSpec.benign(TargetCategory.GRAVEYARD_CARD)
                : TargetSpec.benign(TargetCategory.ANY_GRAVEYARD_CARD);
    }
}
