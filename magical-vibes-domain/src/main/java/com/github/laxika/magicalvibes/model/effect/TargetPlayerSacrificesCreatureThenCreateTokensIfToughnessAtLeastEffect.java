package com.github.laxika.magicalvibes.model.effect;

/**
 * The target player sacrifices a creature of their choice, then the effect controller creates
 * either the normal or increased number of token copies based on the sacrificed creature's
 * effective toughness.
 */
public record TargetPlayerSacrificesCreatureThenCreateTokensIfToughnessAtLeastEffect(
        CreateTokenEffect tokenTemplate,
        int toughnessThreshold,
        int normalAmount,
        int increasedAmount
) implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.player());
    }
}
