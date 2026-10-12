package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * Static replacement effect: if a card would be put into the controller's own graveyard
 * from anywhere, exile it instead (CR 614.1). Used by Forbidden Crypt. The controller-side
 * counterpart of {@link ExileOpponentCardsInsteadOfGraveyardEffect}. Checked in
 * {@code GraveyardService.addCardToGraveyard()}.
 *
 * @param appliesToTokens when true, tokens that would be put into the controller's graveyard are
 *                        exiled as well (Hades, Sorcerer of Eld: "a card or token"); when false,
 *                        only cards are affected (Forbidden Crypt)
 */
public record ExileOwnCardsInsteadOfGraveyardEffect(boolean appliesToTokens) implements OwnGraveyardExileReplacement {

    /** Card-only form, as used by Forbidden Crypt. */
    public ExileOwnCardsInsteadOfGraveyardEffect() {
        this(false);
    }

    @Override
    public CardPredicate filter() {
        return null;
    }

    @Override
    public boolean exemptWhenCycled() {
        return false;
    }
}
