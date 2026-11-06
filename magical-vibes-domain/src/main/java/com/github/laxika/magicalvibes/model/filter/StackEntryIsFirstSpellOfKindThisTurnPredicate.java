package com.github.laxika.magicalvibes.model.filter;

import java.util.List;

/**
 * Matches a spell that is the first spell of one of {@code kinds} its controller has cast this turn ("if it's the first
 * instant spell, the first sorcery spell, or the first Otter spell other than Alania you've cast this turn" — Alania,
 * Divergent Storm): the spell matches some kind, and no other spell its controller cast this turn matches that kind.
 * Each kind is judged with the predicate's source as "this" ({@link CardIsSelfPredicate}). Copies put onto the stack
 * without being cast never match.
 *
 * @param kinds the kinds of spell, each judged on its own
 */
public record StackEntryIsFirstSpellOfKindThisTurnPredicate(List<CardPredicate> kinds) implements StackEntryPredicate {

    public StackEntryIsFirstSpellOfKindThisTurnPredicate {
        kinds = List.copyOf(kinds);
        if (kinds.isEmpty()) {
            throw new IllegalArgumentException("A first-spell-of-kind predicate names at least one kind");
        }
    }
}
