package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;

/**
 * Discovers a card with mana value at most {@code discoverValue}, offering it for a free cast or
 * putting it into its controller's hand when the cast is declined or unavailable.
 */
public record DiscoverEffect(DynamicAmount discoverValue) implements CardEffect {

    public DiscoverEffect(int discoverValue) {
        this(new Fixed(discoverValue));
    }
}
