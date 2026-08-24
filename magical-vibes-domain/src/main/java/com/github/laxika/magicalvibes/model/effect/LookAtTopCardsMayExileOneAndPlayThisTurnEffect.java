package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * Looks at the top {@code count} cards of the controller's library, then may exile one of them
 * and play it until the end of the turn. The remaining cards are put on the bottom in a random
 * order. When {@code filter} is non-null, only matching cards may be chosen; when
 * {@code withoutPayingManaCost} is true, the chosen card may be cast without paying its mana cost.
 */
public record LookAtTopCardsMayExileOneAndPlayThisTurnEffect(
        DynamicAmount count,
        CardPredicate filter,
        boolean withoutPayingManaCost
) implements CardEffect {

    public LookAtTopCardsMayExileOneAndPlayThisTurnEffect(DynamicAmount count) {
        this(count, null, false);
    }

    public LookAtTopCardsMayExileOneAndPlayThisTurnEffect(int count) {
        this(new Fixed(count), null, false);
    }

    public LookAtTopCardsMayExileOneAndPlayThisTurnEffect(int count, CardPredicate filter,
            boolean withoutPayingManaCost) {
        this(new Fixed(count), filter, withoutPayingManaCost);
    }
}
