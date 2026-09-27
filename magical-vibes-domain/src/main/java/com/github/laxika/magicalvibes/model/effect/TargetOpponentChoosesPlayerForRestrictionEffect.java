package com.github.laxika.magicalvibes.model.effect;

/** Target opponent chooses a player for a temporary cast-and-attack restriction. */
public record TargetOpponentChoosesPlayerForRestrictionEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.harmful(TargetPredicates.player());
    }
}
