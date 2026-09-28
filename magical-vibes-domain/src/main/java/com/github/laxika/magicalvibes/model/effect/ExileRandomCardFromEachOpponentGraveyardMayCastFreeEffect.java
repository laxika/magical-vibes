package com.github.laxika.magicalvibes.model.effect;

/**
 * Exiles one random card from each opponent's graveyard and lets the controller cast the exiled
 * nonland cards without paying their mana costs until end of turn. Each spell cast this way also
 * makes its owner lose life equal to its mana value.
 */
public record ExileRandomCardFromEachOpponentGraveyardMayCastFreeEffect() implements CardEffect {
}
