package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * The targeted opponent chooses a card matching {@link #filter()} from their graveyard and puts
 * it onto the battlefield under the spell controller's control.
 */
public record TargetOpponentChoosesCardFromGraveyardEffect(CardPredicate filter) implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.harmful(TargetPredicates.player());
    }
}
