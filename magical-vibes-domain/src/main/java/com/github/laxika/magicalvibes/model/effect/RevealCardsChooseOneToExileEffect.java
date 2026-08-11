package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;

/**
 * Target player reveals a dynamic number of cards they choose from hand, then the controller
 * chooses one of those cards to exile. When used by a source permanent, the chosen card is tracked
 * with that permanent for source-linked permissions.
 */
public record RevealCardsChooseOneToExileEffect(DynamicAmount revealCount) implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.harmful(TargetPredicates.player());
    }
}
