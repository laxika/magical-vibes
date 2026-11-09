package com.github.laxika.magicalvibes.model.filter;

/**
 * Matches a modal spell ("whenever you cast a modal spell", Riku of Many Paths): a spell whose modes were chosen as it
 * was cast (CR 700.2a), so its stack entry records how many times a mode was chosen for it.
 */
public record StackEntryIsModalSpellPredicate() implements StackEntryPredicate {
}
