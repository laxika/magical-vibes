package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Capability interface for attacker-side static effects that restrict <em>who may block this
 * creature</em> — evasion. Lets the battlefield block-legality query ask "how, if at all, can this
 * attacker be blocked" without knowing the concrete evasion record, mirroring how
 * {@link ProtectionGrantingEffect} abstracts protection.
 *
 * <p>Descriptive only: every method states a fact drawn from the record's existing components, never
 * a score and never behaviour. A record fills in only the facet it carries and inherits the
 * empty/{@code false} defaults for the rest, so a single {@code instanceof BlockabilityRestrictionEffect}
 * match plus a facet read replaces the per-record checks in {@code GameQueryService}. The engine still
 * owns all evaluation — it feeds the returned {@link PermanentPredicate}s to its predicate service and
 * supplies engine-computed facts (attacking-alone, historic-spell-cast) itself.
 *
 * <p>Scope note: this covers the six printed attacker-side evasion shapes ({@code CantBeBlockedEffect},
 * {@code CanBeBlockedOnlyByFilterEffect}, {@code CantBeBlockedByCreaturesMatchingPredicateEffect},
 * {@code CantBeBlockedIfDefenderControlsMatchingPermanentEffect},
 * {@code CantBeBlockedIfControllerCastHistoricSpellThisTurnEffect}, and
 * {@code CantBeBlockedIfAttackingAloneEffect}).
 */
public interface BlockabilityRestrictionEffect extends CardEffect {

    /** Whether this creature simply can't be blocked (unconditional evasion). */
    default boolean cantBeBlocked() {
        return false;
    }

    /**
     * When non-{@code null}, this creature can't be blocked as long as the defending player controls
     * a permanent matching this predicate.
     */
    default PermanentPredicate unblockableIfDefenderControls() {
        return null;
    }

    /**
     * Whether this creature can't be blocked as long as its controller has cast a historic spell this
     * turn (artifacts, legendaries, and Sagas).
     */
    default boolean unblockableIfControllerCastHistoricSpellThisTurn() {
        return false;
    }

    /** Whether this creature can't be blocked while it is attacking alone (CR 509.1). */
    default boolean unblockableWhileAttackingAlone() {
        return false;
    }

    /**
     * When non-{@code null}, this creature can be blocked <em>only</em> by blockers matching this
     * predicate; any other blocker is illegal. Paired with {@link #blockableOnlyByDescription()}.
     */
    default PermanentPredicate blockableOnlyBy() {
        return null;
    }

    /** Human-readable phrase for the {@link #blockableOnlyBy()} restriction (e.g. "artifact creatures"). */
    default String blockableOnlyByDescription() {
        return null;
    }

    /**
     * When non-{@code null}, this creature can't be blocked by creatures matching this predicate
     * (e.g. "can't be blocked by creatures with horsemanship").
     */
    default PermanentPredicate cantBeBlockedByCreaturesMatching() {
        return null;
    }
}
