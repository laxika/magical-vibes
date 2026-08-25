package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * The controller chooses a card matching {@link #filter()} from the targeted player's graveyard
 * and puts it onto the battlefield under their control.
 */
public record TargetPlayerChoosesCardFromGraveyardToBattlefieldEffect(
        CardPredicate filter,
        boolean enterTapped,
        boolean exileIfLeavesBattlefield
) implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.harmful(TargetPredicates.player());
    }
}
