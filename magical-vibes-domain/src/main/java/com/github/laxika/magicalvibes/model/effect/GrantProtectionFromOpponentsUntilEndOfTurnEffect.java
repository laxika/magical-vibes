package com.github.laxika.magicalvibes.model.effect;

/** Grants the target permanent protection from permanents and spells controlled by its opponents until end of turn. */
public record GrantProtectionFromOpponentsUntilEndOfTurnEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.permanent());
    }
}
