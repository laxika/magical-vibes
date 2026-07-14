package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Marker interface for effects that represent additional costs of an activated ability
 * (sacrifice, discard, exile, counter removal, etc.). Cost effects are filtered out
 * during effect snapshotting and excluded from mana ability detection.
 *
 * <p>The facets below let consumers — chiefly the AI's cost valuation and simulation
 * payment-planning — ask "what resource does paying this give up, and how much?" without
 * naming the concrete cost record, mirroring how {@link ManaProducingEffect} abstracts mana
 * production for the AI's mana estimator. They are DESCRIPTIVE and deliberately PARTIAL:
 * every facet returns an existing record component (or a shared constant / neutral default),
 * never a score — the AI keeps its own valuation heuristics. Only the cost records the AI
 * currently reasons about override a facet; all other cost records inherit the neutral
 * defaults, exactly the pre-refactor behavior (the AI never recognized them). Every facet is
 * allocation-free on the hot path, so they are safe inside MCTS rollouts.
 */
public interface CostEffect extends CardEffect {

    /**
     * A predicate selecting which of the payer's battlefield permanents may be chosen to pay
     * this cost (sacrifice a creature / artifact / filtered permanent), or {@code null} when
     * this cost does not consume a payer-chosen battlefield permanent — because it pays a
     * scalar resource (life, counters, cards), sacrifices the source itself
     * (see {@link #consumesSourcePermanent()}), or is not permanent-based. Lets a consumer
     * find an eligible sacrifice/return target uniformly via the engine's predicate evaluation.
     */
    default PermanentPredicate consumedPermanentFilter() {
        return null;
    }

    /**
     * True when paying this cost sacrifices the source permanent itself (e.g. "Sacrifice this
     * creature: ...") rather than a payer-chosen permanent. Distinct from
     * {@link #consumedPermanentFilter()}, which selects among other battlefield permanents.
     */
    default boolean consumesSourcePermanent() {
        return false;
    }

    /**
     * True when paying this cost sacrifices a creature the payer chooses from among the
     * creatures they control (the plain "Sacrifice a creature" shape), which the AI values by
     * the cheapest creature it could give up. Any-permanent / artifact-only sacrifices report
     * {@code false}: the AI historically does not fold their loss into this creature-specific
     * estimate, and this facet preserves that.
     */
    default boolean sacrificesChosenCreature() {
        return false;
    }

    /**
     * The life paid to satisfy this cost given the payer's current life total (a fixed amount,
     * or "half your life rounded up"), or {@code 0} when this cost costs no life.
     */
    default int lifePaid(int currentLife) {
        return 0;
    }

    /**
     * The number of counters removed from the source permanent to pay this cost, or {@code 0}
     * when this cost removes none from the source.
     */
    default int sourceCountersRemoved() {
        return 0;
    }

    /**
     * The exact number of cards this cost exiles from the payer's graveyard, or {@code 0} when
     * this cost consumes no graveyard cards. When positive, {@link #consumedGraveyardCardType()}
     * gives the type those cards must have.
     */
    default int consumedGraveyardCardCount() {
        return 0;
    }

    /**
     * The card type the graveyard cards consumed by this cost must have, or {@code null} for any
     * type (only meaningful when {@link #consumedGraveyardCardCount()} is positive).
     */
    default CardType consumedGraveyardCardType() {
        return null;
    }
}
