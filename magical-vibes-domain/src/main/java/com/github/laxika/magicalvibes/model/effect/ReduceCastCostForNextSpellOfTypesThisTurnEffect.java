package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.amount.DynamicAmount;

import java.util.Set;

/** Grants a generic-mana reduction to the next matching spell cast by the controller this turn. */
public record ReduceCastCostForNextSpellOfTypesThisTurnEffect(
        Set<CardType> cardTypes,
        DynamicAmount amount
) implements CardEffect {

    public ReduceCastCostForNextSpellOfTypesThisTurnEffect {
        cardTypes = Set.copyOf(cardTypes);
    }
}
