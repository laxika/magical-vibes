package com.github.laxika.magicalvibes.model.effect;

/**
 * Static replacement effect: if a card would be put into an opponent's graveyard
 * from anywhere, exile it instead (CR 614.1). Used by Leyline of the Void.
 */
public record ExileOpponentCardsInsteadOfGraveyardEffect() implements CardEffect {
}
