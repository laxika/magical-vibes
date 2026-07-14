package com.github.laxika.magicalvibes.model.effect;

public record AwardAnyColorManaEffect(int amount) implements ManaProducingEffect {

    public AwardAnyColorManaEffect() {
        this(1);
    }

    @Override
    public boolean estimatedCountsAllColors() {
        return true;
    }

    @Override
    public int estimatedWildcardMana() {
        return amount;
    }
}
