package com.github.laxika.magicalvibes.model.effect;

/**
 * Static effect: only the controller of this permanent can cast spells during their own turn.
 * Unlike {@link PlayersCanCastSpellsOnlyDuringOwnTurnEffect}, this does not restrict opponents.
 */
public record ControllerCanCastSpellsOnlyDuringOwnTurnEffect() implements CardEffect {
}
