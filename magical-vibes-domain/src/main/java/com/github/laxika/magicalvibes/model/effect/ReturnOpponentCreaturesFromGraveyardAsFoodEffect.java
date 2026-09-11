package com.github.laxika.magicalvibes.model.effect;

/**
 * Returns opponent creature cards put into graveyards from the battlefield this turn under the
 * effect controller's control, making each returned permanent a Food artifact.
 */
public record ReturnOpponentCreaturesFromGraveyardAsFoodEffect() implements CardEffect {
}
