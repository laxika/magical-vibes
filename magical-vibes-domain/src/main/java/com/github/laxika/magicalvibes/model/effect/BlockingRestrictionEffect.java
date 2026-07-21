package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.condition.Condition;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Capability interface for static effects that restrict <em>what a creature may block</em> — both the
 * creature-level "can't block"/"can block only X" shapes and the board-wide "creatures matching X can't
 * block creatures matching Y" shape. Lets the battlefield block-legality query ask "does a blocking
 * restriction apply" without knowing the concrete record, mirroring how
 * {@link ProtectionGrantingEffect} abstracts protection.
 *
 * <p>Descriptive only: every method states a fact drawn from the record's existing components, never a
 * score and never behaviour. A record fills in only the facet it carries and inherits the
 * empty/{@code false} defaults for the rest. The engine still owns all evaluation — it feeds the
 * returned {@link PermanentPredicate}s to its predicate service itself.
 *
 * <p>Scope note: this covers the printed blocker-side restriction shapes ({@code CantBlockEffect},
 * {@code CanBlockOnlyIfAttackerMatchesPredicateEffect}, and the board-wide
 * {@code MatchingCreaturesCantBlockMatchingCreaturesEffect}). The combined "can't attack or block"
 * shapes carry both an attack and a block facet and live on {@link AttackOrBlockRestrictionEffect}.
 */
public interface BlockingRestrictionEffect extends CardEffect {

    /** Whether the creature carrying this effect simply can't block. */
    default boolean cantBlock() {
        return false;
    }

    /**
     * When non-{@code null}, the creature carrying this effect can't block unless this condition is met
     * (the block-only counterpart of {@code CantAttackUnlessEffect} and the block half of
     * {@code CantAttackOrBlockUnlessEffect}). Evaluated in {@code GameQueryService.canBlock} via the
     * condition service, relative to the source permanent's controller.
     */
    default Condition cantBlockUnless() {
        return null;
    }

    /**
     * Whether the creature carrying this effect can't block attackers whose effective power is equal to
     * or greater than this creature's own effective toughness (Ironclaw Curse). Self-referential, so the
     * engine resolves the comparison at block time rather than via a fixed predicate.
     */
    default boolean cantBlockCreaturesWithPowerAtLeastOwnToughness() {
        return false;
    }

    /**
     * When non-{@code null}, the creature carrying this effect can't block attackers whose effective
     * power is equal to or greater than this fixed threshold (Ironclaw Orcs: power 2 or greater). A hard
     * restriction, resolved against the attacker's effective power at block time.
     */
    default Integer cantBlockCreaturesWithPowerAtLeast() {
        return null;
    }

    /**
     * When non-{@code null}, the creature carrying this effect can block <em>only</em> attackers
     * matching this predicate. Paired with {@link #canBlockOnlyAttackersDescription()}.
     */
    default PermanentPredicate canBlockOnlyAttackersMatching() {
        return null;
    }

    /** Human-readable phrase for the {@link #canBlockOnlyAttackersMatching()} restriction. */
    default String canBlockOnlyAttackersDescription() {
        return null;
    }

    /**
     * Board-wide restriction — the "blocker" side: when non-{@code null}, creatures matching this
     * predicate can't block creatures matching {@link #globalCantBlockAttackerMatcher()} (e.g. Boldwyr
     * Intimidator: "Cowards can't block Warriors."). Both matchers are non-{@code null} together.
     */
    default PermanentPredicate globalCantBlockBlockerMatcher() {
        return null;
    }

    /** Board-wide restriction — the "attacker" side matcher; see {@link #globalCantBlockBlockerMatcher()}. */
    default PermanentPredicate globalCantBlockAttackerMatcher() {
        return null;
    }

    /** Human-readable phrase for the board-wide {@link #globalCantBlockBlockerMatcher()} restriction. */
    default String globalCantBlockDescription() {
        return null;
    }
}
