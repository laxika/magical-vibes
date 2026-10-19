package com.github.laxika.magicalvibes.model.effect;

/**
 * Exiles a random card from the controller's library and grants permission to play it until end of
 * turn. When {@code withoutPayingManaCost} is {@code true} the play is free.
 *
 * @param withoutPayingManaCost whether the exiled card may be played without paying its mana cost
 */
public record ExileRandomCardFromLibraryMayPlayThisTurnEffect(boolean withoutPayingManaCost) implements CardEffect {
}
