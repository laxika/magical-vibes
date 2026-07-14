package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Capability interface for static effects that let a creature <em>attack as though it didn't have
 * defender</em>. Covers both the self grant (the carrier itself may attack) and the board-wide grant
 * (every creature matching a predicate may attack), so {@code GameQueryService.canAttackDespiteDefender}
 * can ask "does this effect confer the permission" without knowing the concrete record, mirroring how
 * {@link ProtectionGrantingEffect} abstracts protection.
 *
 * <p>Descriptive only: every method states a fact drawn from the record's existing components, never a
 * score and never behaviour. A record fills in only the facet it carries and inherits the
 * {@code false}/{@code null} defaults for the rest. The engine still owns all evaluation — it feeds the
 * returned {@link PermanentPredicate} to its predicate service itself.
 *
 * <p>Scope note: this covers the self grant {@code CanAttackAsThoughNoDefenderEffect} (typically wrapped
 * in a {@link ConditionalEffect}, e.g. metalcraft) and its board-wide analogue
 * {@code MatchingCreaturesCanAttackAsThoughNoDefenderEffect} (e.g. Rolling Stones for Wall creatures).
 */
public interface NoDefenderAttackPermissionEffect extends CardEffect {

    /** Whether this effect lets the permanent that carries it attack as though it had no defender. */
    default boolean grantsCarrierAttackAsThoughNoDefender() {
        return false;
    }

    /**
     * When non-{@code null}, this effect lets every creature matching this predicate (any controller)
     * attack as though it had no defender.
     */
    default PermanentPredicate noDefenderAttackMatcher() {
        return null;
    }
}
