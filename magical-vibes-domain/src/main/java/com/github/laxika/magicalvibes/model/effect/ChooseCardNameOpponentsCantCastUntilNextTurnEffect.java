package com.github.laxika.magicalvibes.model.effect;

/**
 * Spell effect: choose a card name; until your next turn, your opponents can't cast spells with
 * the chosen name (Comply). Prompts for the name on resolution; the lock is stamped on
 * {@code GameData.opponentsCantCastNamedSpellsUntilControllerNextTurn} and cleared at the start of
 * the controller's next turn.
 */
public record ChooseCardNameOpponentsCantCastUntilNextTurnEffect() implements CardEffect {
}
