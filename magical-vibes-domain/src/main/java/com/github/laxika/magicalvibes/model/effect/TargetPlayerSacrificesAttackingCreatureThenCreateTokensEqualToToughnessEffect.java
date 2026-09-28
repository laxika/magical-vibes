package com.github.laxika.magicalvibes.model.effect;

/**
 * "Target player sacrifices an attacking creature of their choice. You create X tokens, where X
 * is that creature's toughness." (Entrapment Maneuver.)
 *
 * <p>The target player chooses an attacking creature at resolution. Its effective toughness is
 * captured before it leaves the battlefield, while the effect controller creates the tokens.
 */
public record TargetPlayerSacrificesAttackingCreatureThenCreateTokensEqualToToughnessEffect(
        CreateTokenEffect tokenTemplate) implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.player());
    }
}
