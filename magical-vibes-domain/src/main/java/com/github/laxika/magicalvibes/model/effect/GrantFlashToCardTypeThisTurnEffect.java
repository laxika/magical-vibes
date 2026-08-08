package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardType;

/**
 * One-shot effect: the resolving controller may cast spells of the given type this turn as though
 * they had flash. The permission lasts until end of turn and applies to every matching spell.
 */
public record GrantFlashToCardTypeThisTurnEffect(CardType cardType) implements CardEffect {
}
