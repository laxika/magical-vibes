package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;

/**
 * Amass a creature type: create an Army of that type when necessary, then put {@code count}
 * +1/+1 counters on an Army and make that Army the chosen creature type.
 */
public record AmassGoblinsEffect(DynamicAmount count, CardSubtype subtype) implements CardEffect {

    public AmassGoblinsEffect(DynamicAmount count) {
        this(count, CardSubtype.GOBLIN);
    }

    public AmassGoblinsEffect(int count) {
        this(new Fixed(count), CardSubtype.GOBLIN);
    }

    public AmassGoblinsEffect(int count, CardSubtype subtype) {
        this(new Fixed(count), subtype);
    }

    public AmassGoblinsEffect {
        if (count == null) {
            throw new IllegalArgumentException("Amass count must not be null");
        }
        if (subtype == null) {
            throw new IllegalArgumentException("Amass subtype must not be null");
        }
        if (count instanceof Fixed fixed && fixed.value() < 0) {
            throw new IllegalArgumentException("Amass count must not be negative");
        }
    }
}
