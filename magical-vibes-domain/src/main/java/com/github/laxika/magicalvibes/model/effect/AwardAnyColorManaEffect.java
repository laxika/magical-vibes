package com.github.laxika.magicalvibes.model.effect;

public record AwardAnyColorManaEffect(int amount) implements ManaProducingEffect {

    public AwardAnyColorManaEffect() {
        this(1);
    }
}
