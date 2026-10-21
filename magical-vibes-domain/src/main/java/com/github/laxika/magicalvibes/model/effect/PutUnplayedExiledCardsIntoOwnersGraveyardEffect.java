package com.github.laxika.magicalvibes.model.effect;

/**
 * Non-targeting delayed cleanup that puts every card listed in the stack entry's {@code targetCardIds}
 * that is still in exile into its owner's graveyard, as one resolution (Three Wishes: "put any of those
 * cards you didn't play into your graveyard"). Unlike {@link PutTargetCardFromExileIntoOwnersGraveyardEffect}
 * the cards are not targeted, so face-down cards are put into the graveyard as well.
 */
public record PutUnplayedExiledCardsIntoOwnersGraveyardEffect() implements CardEffect {
}
