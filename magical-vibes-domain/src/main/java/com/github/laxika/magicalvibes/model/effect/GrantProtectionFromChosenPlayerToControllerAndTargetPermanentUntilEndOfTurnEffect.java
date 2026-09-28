package com.github.laxika.magicalvibes.model.effect;

/** Gives the ability's controller and its target permanent protection from the remembered player until end of turn. */
public record GrantProtectionFromChosenPlayerToControllerAndTargetPermanentUntilEndOfTurnEffect()
        implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.permanent());
    }
}
