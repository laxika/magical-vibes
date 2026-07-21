package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardHasCyclingPredicate;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * Static replacement: if a card with cycling would be put into your graveyard from anywhere and
 * it wasn't cycled, exile it instead. Used by Abandoned Sarcophagus.
 */
public record ExileOwnCyclingCardsUnlessCycledEffect() implements OwnGraveyardExileReplacement {

    @Override
    public CardPredicate filter() {
        return new CardHasCyclingPredicate();
    }

    @Override
    public boolean exemptWhenCycled() {
        return true;
    }

    @Override
    public boolean appliesToTokens() {
        return false;
    }
}
