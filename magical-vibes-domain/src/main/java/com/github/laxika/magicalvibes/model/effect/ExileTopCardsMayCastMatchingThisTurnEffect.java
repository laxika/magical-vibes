package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * Exile the top {@code count} cards of your library. Until end of turn, you may cast each
 * exiled card that matches {@code filter}; matching cards are cast normally unless
 * {@code withoutPayingManaCost} is true. Timing restrictions still apply in either case.
 * Non-matching cards stay exiled with no play permission.
 * <p>
 * Used by Chandra, Dressed to Kill: +1 with count 1 + red filter ("If it's red, you may cast
 * it this turn"); −7 with count 5 + red filter ("You may cast red spells from among them this
 * turn"). Vance's Blasting Cannons passes a negated land filter for "If it's a nonland card,
 * you may cast that card this turn". Narset, Enlightened Master sets
 * {@code withoutPayingManaCost} for its noncreature, nonland filter.
 * <p>
 * Grants {@code exilePlayPermissions} + {@code exilePlayPermissionsExpireEndOfTurn} for each
 * matching card, and {@code exilePlayWithoutPayingManaCost} when the free variant is used.
 * A {@code null} filter means every exiled card matches —
 * {@code PredicateEvaluationService.matchesCardPredicate} treats a null predicate as always
 * matching — which makes this the unfiltered "you may play those cards this turn" wording as
 * well. {@link ExileTopCardMayPlayThisTurnEffect} remains separate for its unfiltered play
 * wording and existing single-purpose callers.
 */
public record ExileTopCardsMayCastMatchingThisTurnEffect(
        int count,
        CardPredicate filter,
        boolean withoutPayingManaCost
) implements CardEffect {

    public ExileTopCardsMayCastMatchingThisTurnEffect(int count, CardPredicate filter) {
        this(count, filter, false);
    }
}
