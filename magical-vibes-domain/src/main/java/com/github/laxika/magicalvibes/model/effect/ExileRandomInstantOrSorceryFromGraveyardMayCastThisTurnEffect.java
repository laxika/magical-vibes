package com.github.laxika.magicalvibes.model.effect;

/**
 * Exiles a random instant or sorcery card from the controller's graveyard and lets that player
 * cast it this turn. The spell is exiled instead of being put into a graveyard after it is cast.
 */
public record ExileRandomInstantOrSorceryFromGraveyardMayCastThisTurnEffect() implements CardEffect {
}
