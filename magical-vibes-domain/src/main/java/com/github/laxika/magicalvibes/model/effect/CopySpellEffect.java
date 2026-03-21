package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.StackEntryPredicate;

/**
 * Copies the target spell on the stack.
 *
 * @param spellFilter optional predicate restricting which spells can be targeted
 *                    (used by ETB triggers like Naru Meha; null = any spell)
 */
public record CopySpellEffect(StackEntryPredicate spellFilter) implements CardEffect {

    /** No-filter form — used by spells like Twincast where the filter is on the Card's SpellTarget. */
    public CopySpellEffect() { this(null); }

    @Override public boolean canTargetSpell() { return true; }
}
