package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.StackEntryPredicate;

/**
 * Copies the target spell on the stack.
 *
 * @param spellFilter        optional predicate restricting which spells can be targeted
 *                           (used by ETB triggers like Naru Meha; null = any spell)
 * @param tokenWithHaste     when true, the copy is treated as a token that gains haste. Used when
 *                           copying a creature spell (Choreographed Sparks); also suppresses the
 *                           "choose new targets for the copy" prompt.
 * @param sacrificeAtEndStep when true, the token copy is sacrificed at the beginning of the next
 *                           end step (Choreographed Sparks' creature-copy mode).
 * @param copyForTargetController when true, the copy is controlled by the controller of the
 *                                target spell instead of the controller of the copying effect
 *                                (Meletis Charlatan).
 */
public record CopySpellEffect(StackEntryPredicate spellFilter, boolean tokenWithHaste,
                              boolean sacrificeAtEndStep, boolean copyForTargetController) implements CardEffect {

    /** No-filter form — used by spells like Twincast where the filter is on the Card's SpellTarget. */
    public CopySpellEffect() { this(null, false, false, false); }

    /** Filter-only form — used by ETB copy triggers like Naru Meha. */
    public CopySpellEffect(StackEntryPredicate spellFilter) { this(spellFilter, false, false, false); }

    /** Existing full form for ordinary and creature-spell copies. */
    public CopySpellEffect(StackEntryPredicate spellFilter, boolean tokenWithHaste,
                           boolean sacrificeAtEndStep) {
        this(spellFilter, tokenWithHaste, sacrificeAtEndStep, false);
    }

    /** Form for effects that give the target spell's controller control of the copy. */
    public static CopySpellEffect forTargetSpellController() {
        return new CopySpellEffect(null, false, false, true);
    }

    @Override public TargetSpec targetSpec() { return TargetSpec.benign(TargetPredicates.spellOnStack()); }
}
