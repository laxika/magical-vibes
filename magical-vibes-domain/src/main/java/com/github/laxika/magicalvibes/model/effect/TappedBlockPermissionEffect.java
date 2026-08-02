package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Capability interface for static effects that let tapped creatures block as though they were
 * untapped. The block-legality service evaluates the returned matcher relative to the permanent
 * carrying the effect, so controller-relative permissions remain correct after control changes.
 */
public interface TappedBlockPermissionEffect extends CardEffect {

    /**
     * When non-{@code null}, creatures matching this predicate may block while tapped.
     */
    default PermanentPredicate tappedBlockMatcher() {
        return null;
    }
}
