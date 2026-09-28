package com.github.laxika.magicalvibes.model.effect;

/** Grants the controller and planeswalkers they control protection from the target player until the controller's next turn. */
public record GrantProtectionFromTargetPlayerToControllerAndPlaneswalkersUntilNextTurnEffect()
        implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.player());
    }
}
