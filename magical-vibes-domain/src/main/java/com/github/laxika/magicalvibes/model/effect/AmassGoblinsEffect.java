package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;

/**
 * Amass Goblins: create a Goblin Army when necessary, then put {@code count} +1/+1 counters on
 * an Army and make that Army a Goblin.
 */
public record AmassGoblinsEffect(DynamicAmount count) implements CardEffect {

    public AmassGoblinsEffect(int count) {
        this(new Fixed(count));
    }

    public AmassGoblinsEffect {
        if (count == null) {
            throw new IllegalArgumentException("Amass count must not be null");
        }
        if (count instanceof Fixed fixed && fixed.value() < 0) {
            throw new IllegalArgumentException("Amass count must not be negative");
        }
    }
}
