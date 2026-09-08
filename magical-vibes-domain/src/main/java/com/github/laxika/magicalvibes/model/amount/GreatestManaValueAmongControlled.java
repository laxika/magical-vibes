package com.github.laxika.magicalvibes.model.amount;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * The greatest mana value among permanents the controller controls that match {@code filter}
 * (0 when no controlled permanent matches). When {@code excludeSource} is true, the source
 * permanent is excluded from the calculation. One with the Machine uses
 * {@code PermanentIsArtifactPredicate}.
 */
public record GreatestManaValueAmongControlled(PermanentPredicate filter, boolean excludeSource)
        implements DynamicAmount {

    public GreatestManaValueAmongControlled(PermanentPredicate filter) {
        this(filter, false);
    }
}
