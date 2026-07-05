package com.github.laxika.magicalvibes.model.effect;

/**
 * Look at the top {@code count} cards of your library. Put one of them into your hand and the
 * rest on the bottom of your library (in any order). Used by Stress Dream (count = 2:
 * "Look at the top two cards of your library. Put one of those cards into your hand and the
 * other on the bottom of your library.").
 *
 * @param count number of cards to look at
 */
public record LookAtTopCardsChooseOneToHandRestOnBottomEffect(int count) implements CardEffect {
}
