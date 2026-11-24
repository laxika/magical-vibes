package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentIsUnblockedAttackingPredicate;

/**
 * "[Subject] becomes blocked." Marks each attacking creature the subject names as blocked even though no creature is
 * blocking it (this works on creatures that can't be blocked). CR 509.1h: a creature can become blocked without any
 * creature blocking it, and CR 510.1c leaves it with no blockers to assign combat damage to, so it deals none. Its
 * "becomes blocked" triggers fire, per-blocker triggers do not (there is no blocker).
 *
 * <p>Forms: {@code target()} (the no-argument form; "target unblocked attacking creature becomes blocked", harmful;
 * Dazzling Beauty, Choking Vines' X targets) and a set such as {@code controlledBy(eachPlayer(), attacking)}
 * ("attacking creatures become blocked", Fog Patch), which targets nothing. An already blocked creature is unaffected.
 *
 * @param subject the attacking creatures that become blocked
 */
public record MakeTargetAttackingCreatureBlockedEffect(PermanentSubject subject) implements CardEffect {

    /** "Target unblocked attacking creature becomes blocked." */
    public MakeTargetAttackingCreatureBlockedEffect() {
        this(Subjects.target());
    }

    @Override
    public TargetSpec targetSpec() {
        if (subject instanceof PermanentSubject.Target) {
            return TargetSpec.harmful(TargetPredicates.creature(), new PermanentIsUnblockedAttackingPredicate());
        }
        return TargetSpec.NONE;
    }
}
