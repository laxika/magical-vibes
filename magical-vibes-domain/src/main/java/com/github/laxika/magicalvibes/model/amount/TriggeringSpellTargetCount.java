package com.github.laxika.magicalvibes.model.amount;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/** The number of targets of the spell that caused the current triggered ability matching a filter. */
public record TriggeringSpellTargetCount(PermanentPredicate filter) implements DynamicAmount {
}
