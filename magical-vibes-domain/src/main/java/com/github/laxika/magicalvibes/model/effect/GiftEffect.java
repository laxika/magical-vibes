package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.AbilityZones;

/** Declares that a spell has an optional Gift choice made while it is being cast. */
public record GiftEffect(int maxTargetsWithoutGift) implements CardEffect {

    /** The gift choice functions while the card with gift is on the stack (CR 702.174a). */
    @Override
    public AbilityZones staticAbilityZones() {
        return AbilityZones.STACK;
    }

    public GiftEffect() {
        this(Integer.MAX_VALUE);
    }

    public GiftEffect {
        if (maxTargetsWithoutGift < 0) {
            throw new IllegalArgumentException("maxTargetsWithoutGift cannot be negative");
        }
    }
}
