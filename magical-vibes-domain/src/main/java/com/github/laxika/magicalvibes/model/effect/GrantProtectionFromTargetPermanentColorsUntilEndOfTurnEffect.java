package com.github.laxika.magicalvibes.model.effect;

/** Grants the controller's creatures protection from the colors of a target permanent until end of turn. */
public record GrantProtectionFromTargetPermanentColorsUntilEndOfTurnEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.permanent());
    }
}
