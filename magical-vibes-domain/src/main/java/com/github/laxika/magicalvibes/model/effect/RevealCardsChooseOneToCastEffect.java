package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;

/**
 * "Target player chooses X cards from their hand. You look at those cards and may cast a spell
 * from among them without paying its mana cost."
 */
public record RevealCardsChooseOneToCastEffect(DynamicAmount count) implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.player());
    }
}
