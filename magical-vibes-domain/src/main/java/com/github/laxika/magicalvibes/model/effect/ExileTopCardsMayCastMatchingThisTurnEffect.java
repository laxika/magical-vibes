package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * Exile the top {@code count} cards of your library. Until end of turn, you may cast each
 * exiled card that matches {@code filter} (paying its costs; timing restrictions apply).
 * <p>
 * Used by Chandra, Dressed to Kill: +1 with count 1 + red filter ("If it's red, you may cast
 * it this turn"); −7 with count 5 + red filter ("You may cast red spells from among them this
 * turn"). Non-matching cards stay exiled with no play permission.
 */
public record ExileTopCardsMayCastMatchingThisTurnEffect(
        int count,
        CardPredicate filter
) implements CardEffect {
}
