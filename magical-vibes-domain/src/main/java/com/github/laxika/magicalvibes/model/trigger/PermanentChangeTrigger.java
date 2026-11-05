package com.github.laxika.magicalvibes.model.trigger;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.rules.PermanentChange;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Objects;
import java.util.Set;

/**
 * A permanent phases, transforms or is turned face up. Build it through {@link Triggers}.
 *
 * @param relation how the permanent relates to the ability's source
 * @param filter   what the permanent must be, or {@code null} for any permanent. The source of a "this or another"
 *                 relation matches whatever it is.
 * @param changes  the changes it triggers on, never empty
 */
public record PermanentChangeTrigger(ObjectRelation relation, PermanentPredicate filter, Set<PermanentChange> changes)
        implements Trigger {

    public PermanentChangeTrigger {
        Objects.requireNonNull(relation, "relation");
        if (changes.isEmpty()) {
            throw new IllegalArgumentException("A permanent change trigger needs a change");
        }
        changes = Collections.unmodifiableSet(EnumSet.copyOf(changes));
    }

    /** This trigger with {@code changed} as what the permanent must be. */
    public PermanentChangeTrigger withFilter(PermanentPredicate changed) {
        return new PermanentChangeTrigger(relation, changed, changes);
    }

    /**
     * The trigger pipeline judges its filter and the ability's options (effect-slot-refactor.md step 12a), so no
     * collector reads a legacy view of it (step 14 run 16p).
     */
    @Override
    public boolean collectedAsAbility() {
        return true;
    }

    /** Whether the game looks back in time for it: phasing out (CR 603.10b). */
    public boolean looksBack() {
        return changes.equals(Set.of(PermanentChange.PHASED_OUT));
    }
}
