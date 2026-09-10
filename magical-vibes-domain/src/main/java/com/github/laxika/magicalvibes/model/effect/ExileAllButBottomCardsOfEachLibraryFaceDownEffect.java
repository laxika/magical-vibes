package com.github.laxika.magicalvibes.model.effect;

/** Exiles every card in each player's library except that player's bottom cards, face down. */
public record ExileAllButBottomCardsOfEachLibraryFaceDownEffect(int cardsToKeep)
        implements CardEffect {
}
