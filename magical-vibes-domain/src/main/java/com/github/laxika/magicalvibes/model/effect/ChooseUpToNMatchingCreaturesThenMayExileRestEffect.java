package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Chooses up to a number of matching creatures, then offers to exile all other creatures. If the
 * offer is accepted, the source controller is dealt damage by the follow-up handler.
 */
public record ChooseUpToNMatchingCreaturesThenMayExileRestEffect(
        int maxCount, PermanentPredicate choiceFilter, String choiceName) implements CardEffect {

    public ChooseUpToNMatchingCreaturesThenMayExileRestEffect {
        if (maxCount < 0) {
            throw new IllegalArgumentException("maxCount must not be negative");
        }
        if (choiceFilter == null) {
            throw new IllegalArgumentException("choiceFilter must not be null");
        }
        if (choiceName == null || choiceName.isBlank()) {
            throw new IllegalArgumentException("choiceName must not be blank");
        }
    }
}
