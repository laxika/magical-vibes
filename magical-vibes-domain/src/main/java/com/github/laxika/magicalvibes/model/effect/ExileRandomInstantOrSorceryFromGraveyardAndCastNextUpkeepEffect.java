package com.github.laxika.magicalvibes.model.effect;

/**
 * Exiles a random instant or sorcery card from the controller's graveyard and offers it for a
 * free cast at the beginning of the controller's next upkeep. If cast, the spell is exiled
 * instead of being put into a graveyard.
 */
public record ExileRandomInstantOrSorceryFromGraveyardAndCastNextUpkeepEffect() implements CardEffect {
}
