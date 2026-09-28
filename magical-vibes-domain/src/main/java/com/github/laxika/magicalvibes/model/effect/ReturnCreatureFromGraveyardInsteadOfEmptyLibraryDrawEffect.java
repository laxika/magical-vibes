package com.github.laxika.magicalvibes.model.effect;

/**
 * Static replacement effect: if the controller would draw a card while their library is empty,
 * they return a creature card from their graveyard to the battlefield instead. If they cannot,
 * they lose the game.
 */
public record ReturnCreatureFromGraveyardInsteadOfEmptyLibraryDrawEffect()
        implements EmptyLibraryDrawReplacementEffect {
}
