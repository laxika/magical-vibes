package com.github.laxika.magicalvibes.model.effect;

/**
 * Static replacement effect: if a card would be put into the controller's own graveyard
 * from anywhere, exile it instead (CR 614.1). Used by Forbidden Crypt. The controller-side
 * counterpart of {@link ExileOpponentCardsInsteadOfGraveyardEffect}. Checked in
 * {@code GraveyardService.addCardToGraveyard()}.
 */
public record ExileOwnCardsInsteadOfGraveyardEffect() implements CardEffect {
}
