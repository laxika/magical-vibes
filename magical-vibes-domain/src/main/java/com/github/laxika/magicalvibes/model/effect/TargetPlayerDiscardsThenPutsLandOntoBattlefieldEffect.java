package com.github.laxika.magicalvibes.model.effect;

/**
 * Target player discards a fixed number of cards, then the controller may put one land card
 * discarded this way onto the battlefield tapped under their control.
 *
 * @param discardAmount number of cards the target player discards
 */
public record TargetPlayerDiscardsThenPutsLandOntoBattlefieldEffect(int discardAmount)
        implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.player());
    }
}
