package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/** Seeks up to two matching cards within a dynamic mana-value limit, then chooses one for the battlefield. */
public record SeekTwoCardsToBattlefieldWithinManaValueEffect(
        CardPredicate filter,
        DynamicAmount maxManaValue
) implements CardEffect {
}
