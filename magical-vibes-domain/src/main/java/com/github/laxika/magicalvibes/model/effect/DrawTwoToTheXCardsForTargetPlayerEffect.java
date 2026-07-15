package com.github.laxika.magicalvibes.model.effect;

/**
 * Target player draws 2ˣ cards, where X comes from the stack entry's xValue
 * (2⁰ = 1, 2¹ = 2, 2² = 4, and so on). The target player is stored in the
 * stack entry's targetId field. Used by Mathemagics.
 */
public record DrawTwoToTheXCardsForTargetPlayerEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetCategory.PLAYER);
    }
}
