package com.github.laxika.magicalvibes.model.effect;

/** Seeks cards that share no card type with the triggering card. */
public record SeekCardsNotSharingCardTypeWithTriggeringCardEffect(int count) implements CardEffect {
}
