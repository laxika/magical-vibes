package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Static effect that grants a subtype to permanents matching the given subject.
 * For equipment: "equipped creature is a [color] Zombie" adds the subtype.
 * For auras: "enchanted creature is a [type]" similarly.
 * With {@link Subjects#target()}, it can also be used by a resolving ability to permanently
 * replace the target's creature subtypes when {@code overriding} is true.
 * For global enchantments: "each [filter] is an Equipment" with ALL_PERMANENTS subject.
 *
 * @param subtype    the subtype to grant
 * @param subject      which permanents are affected (EQUIPPED_CREATURE, ENCHANTED_CREATURE, ALL_PERMANENTS, etc.)
 * @param overriding when true, replaces all existing creature subtypes instead of adding (e.g. "is a black Zombie")
 * @param filter     optional predicate to restrict which permanents are affected (used with ALL_PERMANENTS, OWN_PERMANENTS)
 */
public record GrantSubtypeEffect(CardSubtype subtype, PermanentSubject subject, boolean overriding,
                                  PermanentPredicate filter) implements CardEffect {

    public GrantSubtypeEffect(CardSubtype subtype, PermanentSubject subject) {
        this(subtype, subject, false, null);
    }

    public GrantSubtypeEffect(CardSubtype subtype, PermanentSubject subject, boolean overriding) {
        this(subtype, subject, overriding, null);
    }

    @Override
    public TargetSpec targetSpec() {
        return subject instanceof PermanentSubject.Target
                ? TargetSpec.benign(TargetPredicates.permanent())
                : TargetSpec.NONE;
    }
}
