package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;

/**
 * Target player chooses cards from their hand. The controller looks at those cards and may cast
 * one nonland card among them without paying its mana cost.
 */
public record TargetPlayerChoosesCardsFromHandThenMayCastOneEffect(DynamicAmount count)
        implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.harmful(TargetPredicates.player());
    }
}
