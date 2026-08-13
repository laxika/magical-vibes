package com.github.laxika.magicalvibes.model.effect;

/** Grants the target creature protection from creatures controlled by its opponents until end of turn. */
public record GrantProtectionFromOpponentCreaturesUntilEndOfTurnEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.creature());
    }
}
