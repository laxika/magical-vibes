package com.github.laxika.magicalvibes.model.amount;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * The number of targets of the spell that caused the current triggered ability matching a filter.
 * A {@code null} filter counts every target, including players, spells, and graveyard cards.
 */
public record TriggeringSpellTargetCount(PermanentPredicate filter) implements DynamicAmount {

    public static TriggeringSpellTargetCount allTargets() {
        return new TriggeringSpellTargetCount(null);
    }
}
