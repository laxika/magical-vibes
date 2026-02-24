package com.github.laxika.magicalvibes.model.effect;

public record SacrificeAttackingCreaturesEffect(int baseCount, int metalcraftCount) implements CardEffect {
    @Override
    public boolean canTargetPlayer() { return true; }
}
