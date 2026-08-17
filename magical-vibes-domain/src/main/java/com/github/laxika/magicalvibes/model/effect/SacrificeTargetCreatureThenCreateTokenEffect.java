package com.github.laxika.magicalvibes.model.effect;

/**
 * The controller of the target creature sacrifices it, then creates the token represented by
 * {@code tokenTemplate}.
 */
public record SacrificeTargetCreatureThenCreateTokenEffect(CreateTokenEffect tokenTemplate)
        implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.harmful(TargetPredicates.creature());
    }
}
