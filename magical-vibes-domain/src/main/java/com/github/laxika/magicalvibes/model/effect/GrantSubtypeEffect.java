package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Permanent;
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
 * @param chosen     where the subtype comes from: {@code subtype} itself, or the subtype the source chose as it
 *                   entered ("enchanted land is the chosen type", Convincing Mirage), or its second chosen one
 *                   ("basic lands of the first chosen type are the second chosen type", Illusionary Terrain); a
 *                   chosen subtype is read from the source as each layered pass applies the effect
 */
public record GrantSubtypeEffect(CardSubtype subtype, PermanentSubject subject, boolean overriding,
                                  PermanentPredicate filter, ChosenSubtype chosen) implements CardEffect {

    /** Where a {@link GrantSubtypeEffect}'s subtype comes from. */
    public enum ChosenSubtype {
        /** The effect's own {@code subtype}. */
        FIXED,
        /** The subtype the source chose as it entered ({@code Permanent.chosenSubtype}). */
        SOURCE_CHOSEN,
        /** The second subtype the source chose as it entered ({@code Permanent.secondChosenSubtype}). */
        SOURCE_SECOND_CHOSEN
    }

    public GrantSubtypeEffect {
        chosen = chosen == null ? ChosenSubtype.FIXED : chosen;
    }

    public GrantSubtypeEffect(CardSubtype subtype, PermanentSubject subject, boolean overriding,
                              PermanentPredicate filter) {
        this(subtype, subject, overriding, filter, ChosenSubtype.FIXED);
    }

    public GrantSubtypeEffect(CardSubtype subtype, PermanentSubject subject) {
        this(subtype, subject, false, null);
    }

    public GrantSubtypeEffect(CardSubtype subtype, PermanentSubject subject, boolean overriding) {
        this(subtype, subject, overriding, null);
    }

    /** "[Permanents] [are] the chosen type": the subtype the source chose as it entered. */
    public static GrantSubtypeEffect chosenSubtype(PermanentSubject subject, boolean overriding,
                                                   PermanentPredicate filter) {
        return new GrantSubtypeEffect(null, subject, overriding, filter, ChosenSubtype.SOURCE_CHOSEN);
    }

    /** "[Permanents] [are] the second chosen type": the source's second as-enters choice. */
    public static GrantSubtypeEffect secondChosenSubtype(PermanentSubject subject, boolean overriding,
                                                         PermanentPredicate filter) {
        return new GrantSubtypeEffect(null, subject, overriding, filter, ChosenSubtype.SOURCE_SECOND_CHOSEN);
    }

    /** The subtype it grants for the given source, or {@code null} when a chosen subtype hasn't been chosen. */
    public CardSubtype subtypeFor(Permanent source) {
        return switch (chosen) {
            case FIXED -> subtype;
            case SOURCE_CHOSEN -> source == null ? null : source.getChosenSubtype();
            case SOURCE_SECOND_CHOSEN -> source == null ? null : source.getSecondChosenSubtype();
        };
    }

    @Override
    public TargetSpec targetSpec() {
        return subject instanceof PermanentSubject.Target
                ? TargetSpec.benign(TargetPredicates.permanent())
                : TargetSpec.NONE;
    }
}
