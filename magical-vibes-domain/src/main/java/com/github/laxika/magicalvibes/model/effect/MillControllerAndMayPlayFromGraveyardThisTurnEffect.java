package com.github.laxika.magicalvibes.model.effect;

/**
 * Mills one card from the controller's library into their graveyard, then grants
 * permission to play that card from the graveyard this turn (lands or spells, paying
 * normal costs). Permission expires at end of turn. Used by Ark of Hunger.
 */
public record MillControllerAndMayPlayFromGraveyardThisTurnEffect() implements CardEffect {
}
