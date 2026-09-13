package com.github.laxika.magicalvibes.model.effect;

/**
 * Lets the controller choose up to two instant and/or sorcery cards from their hand and graveyard
 * with total mana value at most six, then cast the chosen cards without paying their mana costs.
 */
public record CastUpToTwoInstantOrSorceriesFromHandOrGraveyardWithoutPayingManaCostEffect()
        implements CardEffect {
}
