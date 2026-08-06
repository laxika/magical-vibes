package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.amount.DynamicAmount;

/**
 * Marker interface for effects that produce mana. Used to identify mana abilities
 * (CR 605.1a) without listing individual effect types.
 *
 * <p>The facets below additionally let the AI's <em>lightweight mana estimator</em>
 * (virtual-pool building, land-play color coverage, mana-ability scoring) describe a
 * producer's output without naming the concrete effect type. They are DESCRIPTIVE and
 * deliberately PARTIAL: only the three producers the estimator directly models —
 * {@link AwardManaEffect} (a fixed single color) and {@link AwardAnyColorManaEffect}, which reports
 * full colour coverage only for its unrestricted flat-amount shape and a bare quantity for the
 * creature-spell and chosen-subtype restrictions — override them. The other, special-routing producers (chosen-player,
 * restricted-bucket, among-controlled, lands-could-produce, double-pool, flashback/instant-
 * sorcery/subtype-restricted, X, one-of-each) keep the neutral defaults and are resolved
 * exactly by {@code PotentialManaService} / {@code ActivatedAbilityExecutionService}; the
 * estimator ignores them, which is the pre-refactor behavior. Every facet returns an existing
 * component or a constant — no per-call allocation, so they are safe on MCTS rollout paths.
 */
public interface ManaProducingEffect extends CardEffect {

    /**
     * The single, fixed mana color this effect adds to its controller's pool, or {@code null}
     * when it does not add one fixed color to the controller (any-color, multi-color, or any
     * special-routing producer). Only {@link AwardManaEffect} reports a color here.
     */
    default ManaColor estimatedManaColor() {
        return null;
    }

    /**
     * The amount paired with {@link #estimatedManaColor()}, as a {@link DynamicAmount}, or
     * {@code null} when {@link #estimatedManaColor()} is {@code null}.
     */
    default DynamicAmount estimatedManaAmount() {
        return null;
    }

    /**
     * True when this effect adds mana of any of the five colors (player's choice) that the
     * estimator counts as full color coverage — the plain {@link AwardAnyColorManaEffect} shape.
     * Spend-restricted any-color producers return {@code false}, which is also what tells the
     * estimator their mana is unspendable for an ordinary cost (see {@link #estimatedWildcardMana()}).
     */
    default boolean estimatedCountsAllColors() {
        return false;
    }

    /**
     * How much mana this effect produces once its color is chosen at resolution
     * (an unrestricted, creature-spell or chosen-subtype {@link AwardAnyColorManaEffect} → its flat
     * amount), or {@code 0} when the estimator models no such output.
     *
     * <p>This is a <em>quantity</em>, not a promise that the mana is spendable. When
     * {@link #estimatedCountsAllColors()} is {@code false} the producer is spend-restricted: the
     * engine pays it into a bucket (subtype-creature, creature-spell-only, …) that an ordinary cost
     * cannot draw from, so virtual-pool and payment-plan builders must not add it to a plain
     * {@link com.github.laxika.magicalvibes.model.ManaPool}. The one consumer that legitimately
     * reads it for a restricted producer is {@code PotentialManaService.estimateLandManaAmount},
     * where a fixed-color replacement (Infernal Darkness) has already rewritten the mana's type and
     * dropped the restriction — only the quantity survives.
     */
    default int estimatedWildcardMana() {
        return 0;
    }

    /**
     * True when the estimator directly models this effect's output (a fixed color, full color
     * coverage, or a colorless wildcard) — i.e. exactly the three simple producers. Special-routing
     * producers return {@code false}.
     */
    default boolean modeledByManaEstimator() {
        return estimatedManaColor() != null || estimatedCountsAllColors() || estimatedWildcardMana() > 0;
    }
}
