package com.github.laxika.magicalvibes.model.effect;

/**
 * The target player reveals their hand, then discards every nonland card whose name appears on
 * another card in that hand.
 */
public record RevealHandAndDiscardDuplicateNonlandCardsEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.player());
    }
}
