package com.github.laxika.magicalvibes.model.effect;

public record DestroyTargetPermanentEffect(boolean cannotBeRegenerated) implements CardEffect {

    public DestroyTargetPermanentEffect() {
        this(false);
    }
}
