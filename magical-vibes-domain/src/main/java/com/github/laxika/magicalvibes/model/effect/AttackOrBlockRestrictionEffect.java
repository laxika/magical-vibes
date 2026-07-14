package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.condition.Condition;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Capability interface for static effects that restrict <em>both attacking and blocking</em>. The
 * attack side is enforced in {@code CombatAttackService} and the block side in {@code GameQueryService};
 * exposing the underlying facts here lets both consumers ask "does this effect forbid attacking/blocking"
 * without knowing the concrete record, mirroring how {@link ProtectionGrantingEffect} abstracts
 * protection.
 *
 * <p>Descriptive only: every method states a fact drawn from the record's existing components, never a
 * score and never behaviour. A record fills in only the facet it carries and inherits the {@code null}
 * defaults for the rest. The engine still owns all evaluation — it feeds the returned
 * {@link PermanentPredicate} to its predicate service and the returned {@link Condition} to its
 * condition service itself.
 *
 * <p>Scope note: this covers the two printed "can't attack or block" shapes — the board-wide
 * {@code MatchingCreaturesCantAttackOrBlockEffect} (a predicate over affected creatures) and the
 * per-creature {@code CantAttackOrBlockUnlessEffect} (a condition that must be met).
 */
public interface AttackOrBlockRestrictionEffect extends CardEffect {

    /**
     * When non-{@code null}, every creature matching this predicate can't attack or block (evaluated
     * board-wide, relative to the source permanent's controller).
     */
    default PermanentPredicate globallyCantAttackOrBlock() {
        return null;
    }

    /**
     * When non-{@code null}, the creature carrying this effect can't attack or block unless this
     * condition is met.
     */
    default Condition cantAttackOrBlockUnless() {
        return null;
    }

    /** Human-readable phrase for the restriction (the affected-creatures phrase or the "unless" clause). */
    default String restrictionDescription() {
        return null;
    }
}
