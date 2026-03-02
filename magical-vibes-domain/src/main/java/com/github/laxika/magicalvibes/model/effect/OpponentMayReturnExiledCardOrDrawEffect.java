package com.github.laxika.magicalvibes.model.effect;

/**
 * Used in the may-ability system when an opponent is asked whether to let
 * the controller have an exiled card. If the opponent declines, the controller
 * draws {@code drawCount} cards instead.
 *
 * @param drawCount number of cards the controller draws if the opponent declines
 */
public record OpponentMayReturnExiledCardOrDrawEffect(int drawCount) implements CardEffect {
}
