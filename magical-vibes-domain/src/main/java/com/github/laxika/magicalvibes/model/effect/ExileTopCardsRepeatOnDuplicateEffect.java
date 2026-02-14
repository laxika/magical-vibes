package com.github.laxika.magicalvibes.model.effect;

/**
 * Exile the top N cards of a player's library. If two or more of those cards have the same name, repeat this process.
 */
public record ExileTopCardsRepeatOnDuplicateEffect(int count) implements CardEffect {
}
