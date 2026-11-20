package com.github.laxika.magicalvibes.ai;

import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.PermanentSubject;
import com.github.laxika.magicalvibes.model.effect.PlayerSubject;
import com.github.laxika.magicalvibes.model.effect.TargetPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourcePermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * The AI's structural reads of the {@link PermanentSubject} a static boost or keyword grant applies to: which kind of
 * subject it is, whose permanents a set names and whether the set includes the source. The scorers ask these
 * questions instead of comparing scope constants, so they read what the engine's resolver resolves.
 */
public final class GrantSubjectShapes {

    private GrantSubjectShapes() {
    }

    /**
     * The subject {@code scope} stands for, or {@code null} for a scope with no subject variant yet (the triggering
     * spell's targets and the sharing creatures, the declared attackers, the tokens just created, the bandmates).
     */
    public static PermanentSubject subjectOf(GrantScope scope) {
        return scope != null && scope.hasSubject() ? scope.subject() : null;
    }

    /** Whether the subject is the effect's target. */
    public static boolean namesTarget(PermanentSubject subject) {
        return subject instanceof PermanentSubject.Target;
    }

    /** Whether the subject is what the source Aura or Equipment is attached to. */
    public static boolean namesAttached(PermanentSubject subject) {
        return subject instanceof PermanentSubject.Attached;
    }

    /**
     * Whether {@code scope} is the creature the source Aura or Equipment is attached to: {@code attached()}, read only
     * when the host is a creature. {@link GrantScope#ENCHANTED_PERMANENT} is {@code attached()} too but applies to a
     * noncreature host as well, and the subject does not tell them apart yet (the engine's attachment-restriction query
     * reads the same distinction), so this is the one scope constant the AI still names.
     */
    public static boolean namesAttachedCreature(GrantScope scope) {
        return namesAttached(subjectOf(scope)) && scope != GrantScope.ENCHANTED_PERMANENT;
    }

    /**
     * Whether {@code grant} gives its keywords to the permanent it targets: it declares a permanent target, which only
     * a {@code target()} subject (or the target-and-sharing form, which has no subject variant yet) does.
     */
    public static boolean grantsToItsTarget(CardEffect grant) {
        return grant.targetSpec().admits(TargetPredicate.Kind.PERMANENT);
    }

    /** Whether the subject is "creatures you control", with or without the source ("other creatures you control"). */
    public static boolean namesCreaturesYouControl(PermanentSubject subject) {
        return subject instanceof PermanentSubject.ControlledBy set
                && set.players() instanceof PlayerSubject.You
                && creatureSetExcludesSource(set.filter()) != null;
    }

    /** Whether the subject is every player's creatures other than the source ("other creatures"). */
    public static boolean namesEachPlayersOtherCreatures(PermanentSubject subject) {
        return subject instanceof PermanentSubject.ControlledBy set
                && set.players() instanceof PlayerSubject.Each each
                && each.relation() == PlayerSubject.Relation.ALL
                && Boolean.TRUE.equals(creatureSetExcludesSource(set.filter()));
    }

    /**
     * Whether the subject is a set the AI scores as an anthem: creatures you control (the source included or not), or
     * every player's other creatures.
     */
    public static boolean namesAnthemSet(PermanentSubject subject) {
        return namesCreaturesYouControl(subject) || namesEachPlayersOtherCreatures(subject);
    }

    /**
     * For a set predicate that names creatures and nothing else, whether it leaves out the source ({@code true} for
     * creature-and-not-the-source, {@code false} for creature alone); {@code null} for any other predicate.
     */
    private static Boolean creatureSetExcludesSource(PermanentPredicate predicate) {
        if (predicate instanceof PermanentIsCreaturePredicate) {
            return false;
        }
        if (!(predicate instanceof PermanentAllOfPredicate allOf) || allOf.predicates().size() != 2) {
            return null;
        }
        boolean creature = false;
        boolean notSource = false;
        for (PermanentPredicate part : allOf.predicates()) {
            if (part instanceof PermanentIsCreaturePredicate) {
                creature = true;
            } else if (part instanceof PermanentNotPredicate not
                    && not.predicate() instanceof PermanentIsSourcePermanentPredicate) {
                notSource = true;
            }
        }
        return creature && notSource ? Boolean.TRUE : null;
    }
}
