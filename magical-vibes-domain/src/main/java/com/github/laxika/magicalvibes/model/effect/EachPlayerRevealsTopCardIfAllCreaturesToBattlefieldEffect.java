package com.github.laxika.magicalvibes.model.effect;

/**
 * Each player reveals the top card of their library. If every card revealed this way is a
 * creature card, those cards are put onto the battlefield under their owners' control.
 *
 * <p>Cards remain on top of their libraries when the condition is not met. A player with an empty
 * library simply reveals no card.
 */
public record EachPlayerRevealsTopCardIfAllCreaturesToBattlefieldEffect() implements CardEffect {
}
