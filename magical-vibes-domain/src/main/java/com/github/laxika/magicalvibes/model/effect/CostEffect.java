package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
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
     * True when paying this cost taps the permanent that granted the activated ability rather
     * than the permanent activating it.
     */
    default boolean tapsGrantingEquipment() {
        return false;
    }

    /**
     * A predicate selecting which of the payer's battlefield permanents may be chosen to pay
     * this cost (sacrifice a creature / artifact / filtered permanent, return a creature to
     * hand, put a counter on a creature), or {@code null} when this cost does not consume a
     * payer-chosen battlefield permanent — because it pays a scalar resource (life, counters,
     * cards), sacrifices the source itself (see {@link #consumesSourcePermanent()}), or is not
     * permanent-based. Lets a consumer find an eligible permanent uniformly via the engine's
     * predicate evaluation. Every spell cost paid through
     * {@code PlayCardRequest.sacrificePermanentId} must override this: an AI that finds no
     * filter sends a null id and the engine rejects the cast.
     */
    default PermanentPredicate consumedPermanentFilter() {
        return null;
    }

    /**
     * The number of untapped permanents chosen through {@link #consumedPermanentFilter()} that
     * paying this cost taps, or {@code null} when the cost does not tap chosen permanents.
     */
    default DynamicAmount tappedPermanentCount() {
        return null;
    }

    /**
     * True when permanents tapped through {@link #tappedPermanentCount()} must be creatures in
     * addition to matching {@link #consumedPermanentFilter()}.
     */
    default boolean tappedPermanentMustBeCreature() {
        return false;
    }

    /**
     * True when the ability's source cannot be chosen through
     * {@link #consumedPermanentFilter()} to pay this cost.
     */
    default boolean excludesSourceFromConsumedPermanents() {
        return false;
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
     * True when paying this cost sacrifices a battlefield permanent chosen by the payer.
     * Creature-specific sacrifice costs inherit this from {@link #sacrificesChosenCreature()};
     * broader sacrifice costs override it directly.
     */
    default boolean sacrificesChosenPermanent() {
        return sacrificesChosenCreature();
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
     * True when the chosen permanent sacrificed to pay this cost is made available as a card
     * snapshot on the activated ability's stack entry.
     */
    default boolean tracksSacrificedCard() {
        return false;
    }

    /**
     * True when paying this cost must preserve the sacrificed permanent's last-known
     * characteristics for a later effect in the same ability.
     */
    default boolean recordsSacrificedPermanentSnapshot() {
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

    /**
     * An additional predicate the graveyard cards consumed by this cost must match, or
     * {@code null} when the type facet is sufficient.
     */
    default CardPredicate consumedGraveyardCardPredicate() {
        return null;
    }
}
