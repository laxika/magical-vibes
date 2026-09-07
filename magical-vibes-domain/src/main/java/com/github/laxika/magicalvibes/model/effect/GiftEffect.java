package com.github.laxika.magicalvibes.model.effect;

/** Declares that a spell has an optional Gift choice made while it is being cast. */
public record GiftEffect(int maxTargetsWithoutGift) implements CardEffect {

    public GiftEffect() {
        this(Integer.MAX_VALUE);
    }

    public GiftEffect {
        if (maxTargetsWithoutGift < 0) {
            throw new IllegalArgumentException("maxTargetsWithoutGift cannot be negative");
        }
    }
}
