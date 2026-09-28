package com.github.laxika.magicalvibes.model.effect;

/** Resolves Expert-Level Safe's private 1, 2, or 3 choices and their matching result. */
public record ExpertLevelSafeEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.player());
    }
}
