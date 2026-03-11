package com.github.laxika.magicalvibes.model.effect;

/**
 * At the beginning of your upkeep, reveal the top card of your library and put that card
 * into your hand. You lose life equal to its mana value.
 *
 * <p>Used by Dark Tutelage (Dark Confidant variant on an enchantment).
 */
public record RevealTopCardPutIntoHandAndLoseLifeEffect() implements CardEffect {
}
