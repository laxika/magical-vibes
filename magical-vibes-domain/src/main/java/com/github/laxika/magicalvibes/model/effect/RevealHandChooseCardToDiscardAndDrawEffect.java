package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * Reveals a target player's hand. The controller may choose a card matching {@code filter}; if
 * they do, that player discards the chosen card and then draws a card.
 */
public record RevealHandChooseCardToDiscardAndDrawEffect(CardPredicate filter) implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.player());
    }
}
