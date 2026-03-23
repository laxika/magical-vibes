package com.github.laxika.magicalvibes.model.effect;

/**
 * Exiles a specified number of target cards from an opponent's graveyard.
 * Each target is chosen at activation time (they go on the stack and can be responded to).
 * The target card IDs are stored in the stack entry's {@code targetCardIds} field.
 *
 * <p>Used by Deadeye Tracker ({@code count=2}).
 *
 * @param count the number of cards to target and exile
 */
public record ExileTargetCardsFromOpponentGraveyardEffect(int count) implements CardEffect {
    @Override public boolean canTargetGraveyard() { return true; }
    @Override public boolean canTargetAnyGraveyard() { return false; }
}
