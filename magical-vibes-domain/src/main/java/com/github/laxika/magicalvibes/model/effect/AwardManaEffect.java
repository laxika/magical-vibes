package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.ManaColor;

public record AwardManaEffect(ManaColor color, int amount) implements ManaProducingEffect {

    public AwardManaEffect(ManaColor color) {
        this(color, 1);
    }
}
