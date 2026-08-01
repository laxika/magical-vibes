package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.StackEntryPredicate;

/**
 * Non-targeting: counter every spell currently on the stack that matches {@code filter}. Used by
 * overloaded Counterflux ("counter each spell you don't control"). The resolving spell is already
 * off the stack when this runs. Abilities are never countered — only spell stack entries. Respects
 * uncounterable / color-protection via {@code CounterSupport.findCounterTarget}.
 */
public record CounterMatchingSpellsEffect(StackEntryPredicate filter) implements CardEffect {

    public CounterMatchingSpellsEffect {
        if (filter == null) {
            throw new IllegalArgumentException("filter is required");
        }
    }
}
