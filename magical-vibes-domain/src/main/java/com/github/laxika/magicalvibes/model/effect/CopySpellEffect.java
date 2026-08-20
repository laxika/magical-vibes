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
 * @param tokenCopy               when true, the copy is treated as a token without gaining haste
 *                                or offering the usual retarget choice (Drafna, Founder of Lat-Nam)
 * @param removeLegendary         when true, the copy does not have the legendary supertype
 */
public record CopySpellEffect(StackEntryPredicate spellFilter, boolean tokenWithHaste,
                              boolean sacrificeAtEndStep, boolean copyForTargetController,
                              boolean tokenCopy, boolean removeLegendary) implements CardEffect {

    /** No-filter form — used by spells like Twincast where the filter is on the Card's SpellTarget. */
    public CopySpellEffect() { this(null, false, false, false, false, false); }

    /** Filter-only form — used by ETB copy triggers like Naru Meha. */
    public CopySpellEffect(StackEntryPredicate spellFilter) { this(spellFilter, false, false, false, false, false); }

    /** Existing full form for ordinary and creature-spell copies. */
    public CopySpellEffect(StackEntryPredicate spellFilter, boolean tokenWithHaste,
                           boolean sacrificeAtEndStep) {
        this(spellFilter, tokenWithHaste, sacrificeAtEndStep, false, false, false);
    }

    /** Full form retained for ordinary copies and copies controlled by the target spell's controller. */
    public CopySpellEffect(StackEntryPredicate spellFilter, boolean tokenWithHaste,
                           boolean sacrificeAtEndStep, boolean copyForTargetController) {
        this(spellFilter, tokenWithHaste, sacrificeAtEndStep, copyForTargetController, false, false);
    }

    /** Form for effects that give the target spell's controller control of the copy. */
    public static CopySpellEffect forTargetSpellController() {
        return new CopySpellEffect(null, false, false, true, false, false);
    }

    /** Form for copying an artifact spell as a token without granting haste. */
    public static CopySpellEffect asToken() {
        return new CopySpellEffect(null, false, false, false, true, false);
    }

    /** Form for copying a creature spell as a nonlegendary token. */
    public static CopySpellEffect asTokenWithoutLegendary() {
        return new CopySpellEffect(null, false, false, false, true, true);
    }

    @Override public TargetSpec targetSpec() { return TargetSpec.benign(TargetPredicates.spellOnStack()); }
}
