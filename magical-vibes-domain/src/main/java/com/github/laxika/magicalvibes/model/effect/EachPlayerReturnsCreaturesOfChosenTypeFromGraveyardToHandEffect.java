package com.github.laxika.magicalvibes.model.effect;

/**
 * Each player chooses a creature type, then may return any number of matching creature cards
 * from their graveyard to their hand. Cards with changeling match every creature type.
 */
public record EachPlayerReturnsCreaturesOfChosenTypeFromGraveyardToHandEffect() implements CardEffect {
}
