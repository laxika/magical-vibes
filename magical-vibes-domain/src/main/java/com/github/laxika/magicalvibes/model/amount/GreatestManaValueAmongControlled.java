package com.github.laxika.magicalvibes.model.amount;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * The greatest mana value among permanents the controller controls that match {@code filter}
 * (0 when no controlled permanent matches). One with the Machine uses
 * {@code PermanentIsArtifactPredicate}.
 */
public record GreatestManaValueAmongControlled(PermanentPredicate filter) implements DynamicAmount {
}
