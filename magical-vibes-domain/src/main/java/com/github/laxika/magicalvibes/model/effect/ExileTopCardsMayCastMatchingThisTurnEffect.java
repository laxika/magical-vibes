package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * Exile the top {@code count} cards of your library. Until end of turn, you may cast each
 * exiled card that matches {@code filter} (paying its costs; timing restrictions apply).
 * Non-matching cards stay exiled with no play permission.
 * <p>
 * Used by Chandra, Dressed to Kill: +1 with count 1 + red filter ("If it's red, you may cast
 * it this turn"); −7 with count 5 + red filter ("You may cast red spells from among them this
 * turn"). Vance's Blasting Cannons passes a negated land filter for "If it's a nonland card,
 * you may cast that card this turn".
 * <p>
 * Grants {@code exilePlayPermissions} + {@code exilePlayPermissionsExpireEndOfTurn} for each
 * matching card. A {@code null} filter means every exiled card matches —
 * {@code PredicateEvaluationService.matchesCardPredicate} treats a null predicate as always
 * matching — which makes this the unfiltered "you may play those cards this turn" wording as
 * well. {@link ExileTopCardMayPlayThisTurnEffect} remains separate only because it can also
 * grant {@code exilePlayWithoutPayingManaCost} (Oracle's Vault's second ability).
 */
public record ExileTopCardsMayCastMatchingThisTurnEffect(
        int count,
        CardPredicate filter
) implements CardEffect {
}
