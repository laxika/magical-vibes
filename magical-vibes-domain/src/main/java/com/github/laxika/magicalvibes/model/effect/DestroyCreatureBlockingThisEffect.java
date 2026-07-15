package com.github.laxika.magicalvibes.model.effect;

public record DestroyCreatureBlockingThisEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.harmful(TargetCategory.CREATURE);
    }
}
