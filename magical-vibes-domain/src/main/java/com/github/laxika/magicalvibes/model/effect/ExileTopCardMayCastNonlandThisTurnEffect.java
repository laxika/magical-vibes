package com.github.laxika.magicalvibes.model.effect;

/**
 * Triggered effect: "Exile the top card of your library. If it's a nonland card,
 * you may cast that card this turn."
 * <p>
 * The card is always exiled face-up regardless of type. If it is a nonland card,
 * the controller receives a temporary play permission that expires at end of turn.
 * Used by Vance's Blasting Cannons and similar impulse-draw upkeep triggers.
 */
public record ExileTopCardMayCastNonlandThisTurnEffect() implements CardEffect {
}
