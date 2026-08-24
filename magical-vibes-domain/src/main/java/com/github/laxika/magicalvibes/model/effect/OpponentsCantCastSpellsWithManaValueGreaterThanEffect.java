package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * Static effect: opponents of the source's controller can't cast matching spells with mana value
 * greater than the evaluated {@code manaValueLimit}. The controller is unrestricted.
 */
public record OpponentsCantCastSpellsWithManaValueGreaterThanEffect(
        DynamicAmount manaValueLimit,
        CardPredicate spellFilter
) implements CardEffect {

    public OpponentsCantCastSpellsWithManaValueGreaterThanEffect(DynamicAmount manaValueLimit) {
        this(manaValueLimit, null);
    }
}
