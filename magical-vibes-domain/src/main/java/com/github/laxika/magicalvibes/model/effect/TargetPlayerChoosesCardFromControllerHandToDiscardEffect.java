package com.github.laxika.magicalvibes.model.effect;

/**
 * The target player chooses a card from the effect controller's revealed hand for that player to
 * discard.
 */
public record TargetPlayerChoosesCardFromControllerHandToDiscardEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.player());
    }
}
