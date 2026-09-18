package com.github.laxika.magicalvibes.model.effect;

/**
 * One-shot requirement that the target creature block every attacking creature it can legally
 * block this turn. The target creature's block capacity is handled separately by the matching
 * can-block-any-number effect.
 */
public record MustBlockEachAttackingCreatureThisTurnEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.creature());
    }
}
