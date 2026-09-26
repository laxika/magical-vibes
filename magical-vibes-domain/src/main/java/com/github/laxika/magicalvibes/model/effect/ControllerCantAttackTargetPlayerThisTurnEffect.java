package com.github.laxika.magicalvibes.model.effect;

/** Prevents the spell's controller from attacking its targeted player until end of turn. */
public record ControllerCantAttackTargetPlayerThisTurnEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.player());
    }
}
