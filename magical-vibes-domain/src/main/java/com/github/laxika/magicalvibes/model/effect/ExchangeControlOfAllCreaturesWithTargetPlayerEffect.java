package com.github.laxika.magicalvibes.model.effect;

/**
 * Exchanges control of all creatures controlled by the spell's controller and target player until
 * end of turn. The target player is supplied by the stack entry's player target.
 */
public record ExchangeControlOfAllCreaturesWithTargetPlayerEffect()
        implements CardEffect, ControlStealingEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.player());
    }

    @Override
    public ControlDuration controlDuration() {
        return ControlDuration.END_OF_TURN;
    }
}
