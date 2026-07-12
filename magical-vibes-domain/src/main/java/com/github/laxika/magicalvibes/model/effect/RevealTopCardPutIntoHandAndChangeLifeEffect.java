package com.github.laxika.magicalvibes.model.effect;

/**
 * Reveal the top card of your library and put that card into your hand, then change your life
 * total by its mana value.
 *
 * @param gainLife {@code true} = gain life equal to the card's mana value (Augury Adept);
 *                 {@code false} = lose life equal to the card's mana value (Dark Tutelage /
 *                 Dark Confidant, Ruin Raider).
 */
public record RevealTopCardPutIntoHandAndChangeLifeEffect(boolean gainLife) implements CardEffect {
}
