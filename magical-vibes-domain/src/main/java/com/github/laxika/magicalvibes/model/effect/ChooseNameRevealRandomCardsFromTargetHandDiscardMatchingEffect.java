package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;

/**
 * The controller chooses a card name, then the target player reveals that many cards at random
 * from their hand. The target player discards every revealed card with the chosen name.
 */
public record ChooseNameRevealRandomCardsFromTargetHandDiscardMatchingEffect(DynamicAmount count)
        implements CardEffect {

    public ChooseNameRevealRandomCardsFromTargetHandDiscardMatchingEffect(int count) {
        this(new Fixed(count));
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.player());
    }
}
