package com.github.laxika.magicalvibes.model.effect;

/**
 * Exiles every card from every opponent's hand. This is not a discard and does not target a
 * player, so it does not cause discard triggers or use a player target.
 */
public record ExileAllOpponentsHandsEffect() implements CardEffect {
}
