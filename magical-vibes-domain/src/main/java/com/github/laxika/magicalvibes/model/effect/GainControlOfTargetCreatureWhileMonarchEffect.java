package com.github.laxika.magicalvibes.model.effect;

/** Gains control of the target creature while the captured monarch remains the monarch. */
public record GainControlOfTargetCreatureWhileMonarchEffect()
        implements MonarchBoundControlEffect, TargetControllerIsTriggeringPlayerEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.creature());
    }
}
